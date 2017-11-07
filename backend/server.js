var http = require('http');
var https = require('https');
var url = require('url');

/*JSON Objects*/
var I_SMA;
var I_EMA;
var I_STOCH;
var I_RSI;

http.createServer((req, res) => {
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type, Content-Length, Accept, X-Requested-With");

    //http://localhost:12345/?type=autocomplete&symbol=AAPL
    var q = url.parse(req.url, true).query;
    var queryType = q.type;
    var symbol = q.symbol;
    console.log("query: " + queryType + " " + symbol);

    if(queryType === "autocomplete"){
        queryFullSymbolName(symbol, res);
    }else if(queryType === "price"){
        queryStockPrice(symbol, res);
        queryAllStockIndicators(symbol); //fetch all indicators
    }else if(queryType === "indicator"){
        // queryAllStockIndicators(symbol);
        var indicator = q.indicator;
        // querySingleIndicator(indicator, res);
        querySingleIndicator(symbol, indicator, res);
    }

}).listen(12345);


/**
 * auto complete function
 * @param {*} symbol 
 * @param {*} res 
 */
function queryFullSymbolName(symbol, res){
    var baseURL = "http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=";
    var queryUrl = baseURL + symbol;
    
    http.get(queryUrl, (resp) => {
        let data = '';
        // A chunk of data has been recieved.
        resp.on('data', (chunk) => {
            data += chunk;
        });
        // The whole response has been received.
        resp.on('end', () => {
            // console.log(JSON.parse(data).explanation);
            jsonObject = JSON.parse(data);
            // console.log(jsonObject);
            result = JSON.stringify(jsonObject);
            res.writeHead(200, {"Content-Type": "text/json"});
            res.write(result);
            res.end();
        });
    }).on("error", (err) => {
        console.log("Error: " + err.message);
    });
}


/**
 * query stock price and volume
 * @param {*} symbol 
 * @param {*} res 
 */
function queryStockPrice(symbol, res){
    var fs = require('fs');
    var data = JSON.parse(fs.readFileSync("stockDetails/" + symbol + ".json"));
    result = JSON.stringify(data);
    res.writeHead(200, {"Content-Type": "text/json"});
    res.write(result);
    res.end();
}


/**
 * First, server fetches all indicators using this function
 * then, cilent querys each indicator from server
 * @param {*} symbol 
 */
function queryAllStockIndicators(symbol){
    //https://https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=MSFT&interval=15min&outputsize=full&apikey=KAGMK7YPZKV0EYYA
    const request = require('request-promise');
    let urls = [];
    let baseURL = "https://www.alphavantage.co/query?function=";

    indicators = ['SMA', 'EMA', 'STOCH', 'RSI', 'ADX', 'CCI', 'BBANDS', 'MACD'];
    indicators.forEach((indicator) => {
        let tmpURL = baseURL + indicator + "&symbol=" +  symbol +
                     "&interval=15min&time_period=10&series_type=close&&apikey=KAGMK7YPZKV0EYYA";
        urls.push(tmpURL);
        console.log(tmpURL);
    });
    
    const promises = urls.map(url => request(url));
    Promise.all(promises)
    .then((data) => {
        //// data = [promise1,promise2]
        // jsonObject = JSON.parse(data);
        // console.log(JSON.parse(data[1]));
        I_SMA = JSON.parse(data[0]);
        I_EMA = JSON.parse(data[1]);
        I_STOCH = JSON.parse(data[2]);
        I_RSI = JSON.parse(data[3]);
        // console.log(I_STOCH);
    })
    .catch(function(err){
        console.error('err', err);
    });
}




function querySingleIndicator(symbol, indicator, res){
    //https://https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=MSFT&interval=15min&outputsize=full&apikey=KAGMK7YPZKV0EYYA
    const request = require('request-promise');
    let urls = [];
    let baseURL = "https://www.alphavantage.co/query?function=";

    let indicatorURL = baseURL + indicator + "&symbol=" +  symbol +
    "&interval=15min&time_period=10&series_type=close&&apikey=KAGMK7YPZKV0EYYA";
    
    https.get(indicatorURL, (resp) => {
        let data = '';
        // A chunk of data has been recieved.
        resp.on('data', (chunk) => {
            data += chunk;
        });
        // The whole response has been received.
        resp.on('end', () => {
            // console.log(JSON.parse(data).explanation);
            jsonObject = JSON.parse(data);
            console.log(jsonObject["Meta Data"]["2: Indicator"]);
            result = JSON.stringify(jsonObject);
            res.writeHead(200, {"Content-Type": "text/json"});
            res.write(result);
            res.end();
        });
    }).on("error", (err) => {
        console.log("Error: " + err.message);
    });
}


// function querySingleIndicator(indicator, res){
//     res.writeHead(200, {"Content-Type": "text/json"});

//     console.log('aaa ' + indicator);
//     console.log('bbb' + typeof(JSON.stringify(I_SMA)));

    
//     // if(indicator === "SMA"){
//     //     res.write(JSON.stringify(I_SMA));
//     // }else if(indicator === "EMA"){
//     //     res.write(JSON.stringify(I_EMA));
//     // }else if(indicator === "STOCH"){
//     //     res.write(JSON.stringify(I_STOCH));
//     // }else if(indicator === "RSI"){
//     //     res.write(JSON.stringify(I_RSI));
//     // }


//     // //locally test
//     var fs = require('fs');
//     if(indicator === "SMA"){
//         var data = JSON.parse(fs.readFileSync("stockDetails/SMA.json"));
//         res.write(JSON.stringify(data));
//     }else if(indicator === "EMA"){
//         var data = JSON.parse(fs.readFileSync("stockDetails/EMA.json"));
//         res.write(JSON.stringify(data));
//     }else if(indicator === "STOCH"){
//         var data = JSON.parse(fs.readFileSync("stockDetails/STOCH.json"));
//         res.write(JSON.stringify(data));
//     }else if(indicator === "RSI"){
//         var data = JSON.parse(fs.readFileSync("stockDetails/RSI.json"));
//         res.write(JSON.stringify(data));
//     }


//     res.end();
// }






// //cilent querys each indicator from this server



