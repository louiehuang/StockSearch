var http = require('http');
var https = require('https');
var url = require('url');
var xml2js = require('xml2js');

var server = http.createServer((req, res) => {
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type,Content-Length, Authorization, Accept,X-Requested-With");
    res.setHeader("Access-Control-Allow-Methods","PUT,POST,GET,DELETE,OPTIONS");

    //http://localhost:12345/?type=autocomplete&symbol=AAPL
    var q = url.parse(req.url, true).query;
    var queryType = q.type;
    var symbol = q.symbol;

    if(queryType === "autocomplete"){
        //http://localhost:12345/?type=autocomplete&symbol=AAPL
        queryFullSymbolName(symbol, res);
    }else if(queryType === "price"){
        //http://localhost:12345/?type=price&symbol=AAPL
        queryStockPrice(symbol, res);
        // locallyQueryStockPrice(symbol, res); //local test
    }else if(queryType === "indicator"){
        //http://localhost:12345/?type=indicator&symbol=AAPL&indicator=SMA
        var indicator = q.indicator;
        // console.log("query: " + queryType + " symbol: " + symbol + " indicator: " + indicator);
        querySingleIndicator(symbol, indicator, res);
        // locallyQuerySingleIndicator(symbol, indicator, res); //local test
    }else if(queryType === "news"){
        //http://localhost:12345/?type=news&symbol=AAPL
        queryNews(symbol, res);
    } else{
        res.writeHead(404, {"Content-Type": "text/plain"});
        res.end("Invalid Format");
    }
});
var port = process.env.PORT || 12345;
server.listen(port);

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
            try {
                jsonObject = JSON.parse(data);
                result = JSON.stringify(jsonObject);
                res.writeHead(200, {"Content-Type": "text/json"});
                res.write(result);
            } catch (error) {
                res.writeHead(404, {"Content-Type": "text/json"});
            }
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
    //https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=MSFT&outputsize=full&apikey=KAGMK7YPZKV0EYYA
    var baseURL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=";
    var queryUrl = baseURL + symbol + "&outputsize=full&apikey=KAGMK7YPZKV0EYYA";
    // console.log(queryUrl);
    
    https.get(queryUrl, (resp) => {
        let data = '';
        resp.on('data', (chunk) => {
            data += chunk;
        });
        resp.on('end', () => {
            try {
                jsonObject = JSON.parse(data);
                result = JSON.stringify(jsonObject);
                res.writeHead(200, {"Content-Type": "text/json"});
                res.write(result);
            } catch (error) {
                res.writeHead(404, {"Content-Type": "text/json"});
            }
            res.end();
        });
    }).on("error", (err) => {
        res.writeHead(404, {"Content-Type": "text/json"});
        res.end();
        console.log("Error: " + err.message);
    });
}


/**
 * client call this function to query indicators
 * @param {*} symbol 
 * @param {*} indicator 
 * @param {*} res 
 */
function querySingleIndicator(symbol, indicator, res){
    //https://www.alphavantage.co/query?function=SMA&symbol=AAPL&interval=15min&time_period=10&series_type=close&&apikey=KAGMK7YPZKV0EYYA
    let baseURL = "https://www.alphavantage.co/query?function=";
    let indicatorURL = baseURL + indicator + "&symbol=" +  symbol +
    "&interval=daily&time_period=10&series_type=close&&apikey=KAGMK7YPZKV0EYYA";
    // console.log(indicatorURL);
    
    https.get(indicatorURL, (resp) => {
        let data = '';
        resp.on('data', (chunk) => {
            data += chunk;
        });
        resp.on('end', () => {
            try {
                jsonObject = JSON.parse(data);
                result = JSON.stringify(jsonObject);
                res.writeHead(200, {"Content-Type": "text/json"});
                res.write(result);
                res.end();
            } catch (error) {
                res.writeHead(404, {"Content-Type": "text/json"});
                // console.log("parse Error: " + error);
            }
        });
    }).on("error", (err) => {
        res.writeHead(404, {"Content-Type": "text/json"});
        res.end();
        console.log("Error: " + err.message);
    });
}



/**
 * fetch xml news, then convert to json, send to client
 * @param {*} symbol 
 */
function queryNews(symbol, res){
    //https://seekingalpha.com/api/sa/combined/AAPL.xml
    let baseURL = "https://seekingalpha.com/api/sa/combined/";
    let newURL = baseURL + symbol + ".xml";
    // console.log(newURL);
    
    https.get(newURL, (resp) => {
        let xmlData = '';
        resp.on('data', (chunk) => {
            xmlData += chunk;
        });
        resp.on('end', () => {
            //xmlData is xml object now, convert is to json
            // console.log(xmlData);
            var parser = new xml2js.Parser();
            var jsonObject;
            parser.parseString(xmlData, (err, result) => {
                if(err){
                    res.writeHead(404, {"Content-Type": "text/json"});
                    res.end();
                    console.log("parse Error: " + err);
                }else{
                    jsonObject = result;
                    var newsJson = jsonObject['rss']['channel'][0]['item'];
                    // console.log(newsJson.length);
                    jsonString = JSON.stringify(newsJson);
                    res.writeHead(200, {"Content-Type": "text/json"});
                    res.write(jsonString);
                    res.end();
                }
            });
        });
    }).on("error", (err) => {
        res.writeHead(404, {"Content-Type": "text/json"});
        res.end();
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
    try {
        var data = JSON.parse(fs.readFileSync("stockDetails/" + symbol + ".json"));
        result = JSON.stringify(data);
        res.writeHead(200, {"Content-Type": "text/json"});
        res.write(result);
    } catch (error) {
        res.writeHead(404, {"Content-Type": "text/json"});
    }
    res.end();
}


function locallyQuerySingleIndicator(symbol, indicator, res){
    var fs = require('fs');
    try {
        var data = JSON.parse(fs.readFileSync("stockDetails/" + indicator + ".json"));
        result = JSON.stringify(data);
        res.writeHead(200, {"Content-Type": "text/json"});
        res.write(result);    
    } catch (error) {
        res.writeHead(404, {"Content-Type": "text/json"});
    }
    // res.writeHead(404, {"Content-Type": "text/json"});
    res.end();
}
