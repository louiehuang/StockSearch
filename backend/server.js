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

    if(queryType === "autocomplete"){
        queryFullSymbolName(symbol, res);
    }else if(queryType === "price"){
        
        queryStockPrice(symbol, res);

        // locallyQueryStockPrice(symbol, res);

    }else if(queryType === "indicator"){
        var indicator = q.indicator;
        console.log("query: " + queryType + " symbol: " + symbol + " indicator: " + indicator);
        
        querySingleIndicator(symbol, indicator, res);

        // locallyQuerySingleIndicator(symbol, indicator, res); //local test

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
    //https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=MSFT&outputsize=full&apikey=KAGMK7YPZKV0EYYA"
    var baseURL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=";
    var queryUrl = baseURL + symbol + "&outputsize=full&apikey=KAGMK7YPZKV0EYYA";
    console.log(queryUrl);
    
    https.get(queryUrl, (resp) => {
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
function locallyQueryStockPrice(symbol, res){
    var fs = require('fs');
    var data = JSON.parse(fs.readFileSync("stockDetails/" + symbol + ".json"));
    result = JSON.stringify(data);
    res.writeHead(200, {"Content-Type": "text/json"});
    res.write(result);
    res.end();
}

/**
 * client call this function to query indicators
 * @param {*} symbol 
 * @param {*} indicator 
 * @param {*} res 
 */
function querySingleIndicator(symbol, indicator, res){
    //https://www.alphavantage.co/query?function=SMA&symbol=AAPL&interval=15min&time_period=10&series_type=close&&apikey=KAGMK7YPZKV0EYYA
    const request = require('request-promise');
    let urls = [];
    let baseURL = "https://www.alphavantage.co/query?function=";

    let indicatorURL = baseURL + indicator + "&symbol=" +  symbol +
    "&interval=daily&time_period=10&series_type=close&&apikey=KAGMK7YPZKV0EYYA";
    console.log(indicatorURL);
    
    https.get(indicatorURL, (resp) => {
        let data = '';
        // A chunk of data has been recieved.
        resp.on('data', (chunk) => {
            data += chunk;
        });
        // The whole response has been received.
        resp.on('end', () => {
            // console.log(JSON.parse(data).explanation);
            try {
                jsonObject = JSON.parse(data);
                // console.log(jsonObject);
                result = JSON.stringify(jsonObject);
                res.writeHead(200, {"Content-Type": "text/json"});
                res.write(result);
                res.end();
            } catch (error) {
                console.log("parse Error: " + error);
            }
        });
    }).on("error", (err) => {
        console.log("Error: " + err.message);
    });
}



function locallyQuerySingleIndicator(symbol, indicator, res){
    var fs = require('fs');
    var data = JSON.parse(fs.readFileSync("stockDetails/" + indicator + ".json"));
    result = JSON.stringify(data);
    res.writeHead(200, {"Content-Type": "text/json"});
    res.write(result);
    res.end();
}


