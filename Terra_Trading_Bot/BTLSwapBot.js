import io from 'fs';
import { LCDClient,Denom, Coin, MnemonicKey, Wallet, Msg, MsgExecuteContract, MsgSwap, TxAPI, AuthAPI, WasmAPI} from '@terra-money/terra.js';

//Envirment setting 
const optionStr = await io.readFileSync('env.txt', 'utf8');
const lineStr = optionStr.split(';');

const BALANCE = 1000000; // Ratio Value 
const BLUNA_BUY_PRICE = lineStr[0].split(':')[1];
const BLUNA_SELL_PRICE = lineStr[1].split(':')[1];
const POSITION = lineStr[2].split(':')[1]; // BOT STATUS
const LUNA_MAX = lineStr[3].split(':')[1]; // available luna max 
const LUNA_START = lineStr[4].split(':')[1];//LUNA START amount
const BLUNA_START = lineStr[5].split(':')[1];//BLUNA START amount

// TESTNET
/*const BLUNA_TOKEN = "terra1u0t35drzyy0mujj8rkdyzhe264uls4ug3wdp3x";
const BLUNA_TOKEN_PAIR = "terra13e4jmcjnwrauvl2fnjdwex0exuzd8zrh5xk29v";*/
// MAINNET 
const BLUNA_TOKEN = "terra1kc87mu460fwkqte29rquh4hc20m54fxwtsx7gp";
const BLUNA_TOKEN_PAIR = "terra1jxazgm67et0ce260kvrpfv50acuushpjsz2y0p";
//TEST NET
/*const KEY = new MnemonicKey({
  mnemonic: "" //  니모닉 기입
});*/
//MAIN NET
const KEY = new MnemonicKey({
  mnemonic: "" //  니모닉 기입 
});

/*const CLIENT = new LCDClient({
  URL: 'https://bombay-lcd.terra.dev',
  chainID: 'bombay-12',
  gasPrices: { uusd: 1.0 }
});*/
const CLIENT = new LCDClient({
  URL: 'https://lcd.terra.dev',
  chainID: 'columbus-5',
  gasPrices: { uusd: 1.0 }
});

const WALLET = new Wallet(CLIENT,KEY);

var luna_ava = LUNA_START; // available luna 
var bluna_ava = BLUNA_START; // available bluna 
var position = ""; // real position 

if(POSITION === "BUY")
    position = "BUY";
else if (POSITION === "SELL")
    position = "SELL";

console.log("BOT INIT...");
console.log("BOT POSITION = "+POSITION);
console.log("BOT ENV = COM");
console.log("BOT encryption sync");
console.log("Well com running");
// BOT_RUNNING
while(true)
{
    //WAIT 
    await sleep(500);

    //BUY POSITION
    if(position === "BUY")
    {
        
        try 
        {
            //simulate 
            var simulate_price = await GetRatio_LunaToBluna(luna_ava); 
            simulate_price = simulate_price / BALANCE;
            let limite_buy_price = (luna_ava / BLUNA_BUY_PRICE).toFixed(0); 
     
            // simulate Compare //  limite_buy_price <= simulate_price
            if(limite_buy_price <= simulate_price)
            {
              
                // swap 
                let allow_execute = Create_IncreaseLunaAmount_Msg(luna_ava);
                let swap_execute = Create_LunaToBluna_Msg(luna_ava,BLUNA_BUY_PRICE);
                         
                const Tx  = await WALLET.createAndSignTx({ 
                    msgs: [allow_execute,swap_execute]
                });

                const result = await WALLET.lcd.tx.broadcast(Tx);
                // loging 
                let today = new Date();
                console.log(today.toLocaleString()+"_BUY_ Luna Amount -> "+luna_ava);
                console.log(today.toLocaleString()+"_BUY_ Belive Simulate Price Amount -> "+simulate_price);
                console.log(today.toLocaleString()+"_BUY_ Limite_buy_price -> "+limite_buy_price);
                console.log(today.toLocaleString()+"_BUY_ 1 Amount Simulate price -> "+simulate_price/1000);
                console.log(today.toLocaleString()+"_BUY_"+result.txhash); // Transaction -> save log 
                 //console.log(bluna_ava / BALANCE); // after buy bluna amount -> save log
                 //console.log(result.raw_log); // evnet log  -> save log  


                //position change 
                position = "SELL";
                bluna_ava = await GetbLunaBalance(); 
                bluna_ava = bluna_ava / BALANCE;
                
            }
        } catch (e) {
            let today = new Date();
            console.log(today.toLocaleString()+"_BUY POSITION_"+e);
            //break;
        } 

    } else if(position === "SELL")//SELL POSITION
    {   
        try
        {

            //simulate 
            var simulate_price = await GetRatio_BlunaToLuna(bluna_ava);
            simulate_price = simulate_price / BALANCE;
            let limite_sell_price = (bluna_ava * BLUNA_SELL_PRICE).toFixed(0); 


             // simulate Compare //  limite_sell_price <= simulate_price
            if(limite_sell_price <= simulate_price)
            {

                //swap
                const sell_excute =  Create_BlunaToLuna_Msg(bluna_ava,BLUNA_SELL_PRICE);
                const Tx  = await WALLET.createAndSignTx({ 
                        msgs: [sell_excute]
                });
                const result = await WALLET.lcd.tx.broadcast(Tx);

                //loging
                let today = new Date();
                console.log(today.toLocaleString()+"_SELL_ Bluna Amount -> "+bluna_ava);
                console.log(today.toLocaleString()+"_SELL_ Belive Simulate Price Amount -> "+simulate_price);
                console.log(today.toLocaleString()+"_SELL_ Limite_buy_price -> "+limite_sell_price);
                console.log(today.toLocaleString()+"_SELL_ 1 Amount Simulate price -> "+simulate_price/1000);
                console.log(today.toLocaleString()+"_SELL_"+result.txhash); // Transaction -> save log 
                //console.log(luna_ava / BALANCE); // after sell luna amount -> save log
                //console.log(result.raw_log); // evnet log  -> save log 


                // position change 
                position = "BUY";
                luna_ava = await GetLunaBalance();
                luna_ava = luna_ava/BALANCE;
                break;
            }
        }catch(e){
            let today = new Date();
            console.log(today.toLocaleString()+"_SELL POSITION_"+e);
            //break;
        }
    }

}

function sleep(ms) {
  return new Promise(resolve=>setTimeout(resolve, ms));
}
// Luna -> Bluna Simulate //소수점 포함해서 넣어주자 
async function GetRatio_LunaToBluna(amount)
{
    amount = amount * BALANCE; // 소수점 제거
    const obj = await CLIENT.wasm.contractQuery(BLUNA_TOKEN_PAIR,
    {
      "simulation":{
        "offer_asset":{
          "amount": amount.toString(),
          "info":{
            "native_token":{
              "denom":"uluna"
            }
          }
        }
      }
    });
    return obj.return_amount
}

// Bluna -> luna Simulate //소수점 포함해서 넣어주자 
async function GetRatio_BlunaToLuna(amount)
{
    amount = amount * BALANCE;
    const obj = await CLIENT.wasm.contractQuery(BLUNA_TOKEN_PAIR, {
      "simulation":{
        "offer_asset":{
          "amount":amount.toString(),
          "info":{
            "token":{
              "contract_addr": BLUNA_TOKEN.toString()
            }
          }
        }
      }
    });
    return obj.return_amount;
}

// (Previous buy method) increase luna amount
function Create_IncreaseLunaAmount_Msg(amount){
    amount = amount * BALANCE;
    const execute = new MsgExecuteContract(
      WALLET.key.accAddress,
      BLUNA_TOKEN,
      {
        increase_allowance: {
          amount: amount.toString(),
          spender: BLUNA_TOKEN_PAIR
        },
      },[]
    )
    return execute;
}

// BUY Luna -> Bluna 
function Create_LunaToBluna_Msg(amount,bpAmount){
    amount = amount * BALANCE;

    const execute = new MsgExecuteContract(
      WALLET.key.accAddress,
      BLUNA_TOKEN_PAIR,
      {
        swap: {
          offer_asset: {
            info: { 
              native_token: { 
                denom: 'uluna' 
              } 
            },
            amount: amount.toString()
          },
          max_spread: '1',
          belief_price: bpAmount.toString()
        }

      },[new Coin('uluna', amount.toString())]
    );
    return execute;
}

// SELL BLUNA -> LUNA 
function Create_BlunaToLuna_Msg(amount,bpAmount)
{
    amount = amount * BALANCE;
    console.log(amount+"");
    const message = JSON.stringify({
          swap: {
            max_spread: '1', // slipage
            belief_price: bpAmount.toString(), // will have! simulating price ->  bpAmount.toString()
          },
    });

    const execute = new MsgExecuteContract(
      WALLET.key.accAddress,
      BLUNA_TOKEN, // bluna token 
    {
      send:{
        amount : amount.toString(),
        contract: BLUNA_TOKEN_PAIR, // bluna pair token 
        msg : Buffer.from(message, "utf8").toString('base64')
      },
    });

    return execute;
}

// GET Wallet Bluna amount 
async function GetbLunaBalance()
{
    const coin = await CLIENT.wasm.contractQuery(BLUNA_TOKEN,{
        balance: { 
          address: WALLET.key.accAddress.toString()
        }
    });
   return coin.balance;
}

// Get Wallet Luna amount
async function GetLunaBalance()
{
    luna:Coin;
    const balance = await CLIENT.bank.balance(WALLET.key.accAddress);
    const luna = balance.get(Denom.LUNA);
    return luna.amount;
}


