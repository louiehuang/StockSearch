import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import 'rxjs/add/operator/toPromise';
// import * as moment from 'moment';
import * as moment from 'moment-timezone';

@Injectable()
export class ChartsService {
    constructor(private http : Http){
        
    }

    /**
     * parse prive & volume data
     * @param jsonObj 
     */
    parsePriceData(jsonObj){
        var array_date = [], array_price = [], array_volume = [];
        var max_volume = 0, cnt = 0;
        for(var key in jsonObj) {
            if(cnt >= 126)
                break;
            array_date.push(key.substring(5).replace(/-/g, "\/"));
            array_price.push(parseFloat(jsonObj[key]['4. close']));
            var volume = parseFloat(jsonObj[key]['5. volume']);
            array_volume.push(volume);
            max_volume = Math.max(max_volume, volume);
            cnt++;
        }
    
        max_volume *= 1.5;
        
        //remember to reverse
        return {
            date: array_date.reverse(),
            price: array_price.reverse(),
            volume: array_volume.reverse(),
            max_volume: max_volume
        };
    }





    /**
     * Parse indicator with only one target, such as "SMA"
     * @param jsonObj
     * @param target 
     */
    parseSingleTarget(jsonObj, target){
        var array_date = [], array_indicator = [], cnt = 0;
        for(var key in jsonObj) {
            if(cnt++ >= 126) break;
            array_date.push(key.substring(5).replace(/-/g, "\/"));
            array_indicator.push(parseFloat(jsonObj[key][target]));
        }
        return {
            date: array_date.reverse(),
            indicator: array_indicator.reverse()
        };
    }


    /**
     * Parse indicator with two targets, such as "SlowD" & "SlowK"
     * @param jsonObj 
     * @param target1 
     * @param target2 
     */
    parseTwoTarget(jsonObj, target1, target2){
        var array_date = [], array_indicator_1 = [], array_indicator_2 = [], cnt = 0;
        for(var key in jsonObj) {
            if(cnt++ >= 126) break;
            array_date.push(key.substring(5).replace(/-/g, "\/"));
            array_indicator_1.push(parseFloat(jsonObj[key][target1]));
            array_indicator_2.push(parseFloat(jsonObj[key][target2]));
        }
        return {
            date: array_date.reverse(),
            indicator_1: array_indicator_1.reverse(),
            indicator_2: array_indicator_2.reverse(),
        };
    }

        
    /**
     * Parse indicator with three targets, such as:
     * "Real Middle Band", "Real Lower Band", "Real Upper Band"
     * @param jsonObj 
     * @param target1 
     * @param target2 
     * @param target3 
     */
    parseThreearget(jsonObj, target1, target2, target3){
        var array_date = [], cnt = 0;;
        var array_indicator_1 = [], array_indicator_2 = [], array_indicator_3 = [];
        for(var key in jsonObj) {
            if(cnt++ >= 126) break;
            array_date.push(key.substring(5).replace(/-/g, "\/"));
            array_indicator_1.push(parseFloat(jsonObj[key][target1]));
            array_indicator_2.push(parseFloat(jsonObj[key][target2]));
            array_indicator_3.push(parseFloat(jsonObj[key][target3]));
        }
        return {
            date: array_date.reverse(),
            indicator_1: array_indicator_1.reverse(),
            indicator_2: array_indicator_2.reverse(),
            indicator_3: array_indicator_3.reverse(),
        };
    }
  


    parseStockData(jsonObj){
        // var ts = moment("2013-10-31", "YYYY-MM-DD").valueOf();
        // var m = moment(ts);
        // var s = m.format("MM/DD/YYYY");
        // console.log("Values are: ts = " + ts + ", s = " + s);

        var cnt = 0, combinedData = [] //[[ms, price], [ms, price], [ms, price]... [ms, price]]
        var array_date = [], array_price = [];
        for(var key in jsonObj) {
            if(cnt++ >= 1000)
                break;
            var date = moment(key, "YYYY-MM-DD").valueOf(); //key = "2017-10-19"
            array_date.push(date);
            array_price.push(parseFloat(jsonObj[key]['4. close']));
        }
        array_date.reverse();
        array_price.reverse();

        var ret = [];
        for(var i = 0; i < array_date.length; i++){
            var tmp = [];
            tmp.push(array_date[i]); tmp.push(array_price[i]);
            ret.push(tmp);
        }

        return ret;
    }


    /**
     * parse news
     * there are two type links:
     * one contains guid, this is artical link ok to use;
     * one does not contains guid, this is news homepage link, need to do conversion
     * @param data 
     * @param limit 
     */
    parseNew(data, timeZone, limit){
        console.log("timeZone: " + timeZone);
        var ret = [];
        for(var i = 0; i < limit; i++){
            //process link
            var title = data[i]['title'].toString().trim(); //Microsoft focused on AI investments
            var guid = data[i]['guid'][0]['_']; 
            guid = guid.toString().substring(guid.length - 7); 
            //https://seekingalpha.com/news/3301685-microsoft-focused-ai-investments
            //3301685 is in guid, and -microsoft-focused-ai-investments comes from title
            var link = data[i]['link'].toString(); //in AAPL it's artical link, but in MSFT, it's news index
            // console.log(link + ", " + link.indexOf(guid));
            if(link.indexOf(guid) == -1){
                link = "https://seekingalpha.com/news/" + guid;
                title = title.toLowerCase().replace(/\s+/gi, "-");
                link += "-" + title;
                data[i]['link'] = link;
            }

            //process timezone
            data[i]['pubDate'] = this.convertDate(data[i]['pubDate'].toString(), timeZone);

            ret.push(data[i]);
        }
        return ret;
    }


    /**
     * format lookup: http://momentjs.com/docs/#/displaying/format/
     * date: "Tue, 07 Nov 2017 16:01:58 -0500", but this is not RFC2822 or ISO format
     * so, change to "Tue 07 Nov 2017 16:01:58 -0500" (delete comma)
     * timeZone is "US/Eastern"
     * Question!!!!!
     * should return "Tue, 07 Nov 2017 16:01:58 EST" or "Tue, 07 Nov 2017 11:01:58 EST" ???
     * if latter on, date should be "Tue 07 Nov 2017 16:01:58 GMT -0500"
     * @param date "Tue, 07 Nov 2017 16:01:58 -0500"
     * @param timeZone 
     */
    convertDate(date, timeZone){
        var commaIdx = date.indexOf(",");
        date = date.substring(0, commaIdx) + date.substring(commaIdx + 1);
        // date = "Tue 07 Nov 2017 16:01:58 GMT -0500"; //changed to 11:01:58 but get warning!!!
        // console.log(date);

        var time = moment.tz(date, timeZone).format("ddd, DD MMM YYYY HH:mm:ss");
        var timeZoneName = moment.tz(date, timeZone).format('zz'); //EDT or EST

        var convertedTime = time + " " + timeZoneName;
        // console.log(convertedTime);
        return convertedTime;
    }

    /**
     * 
     * @param date "2017-11-07 16:16:23"
     * @param timeZone 
     */
    getTimeZoneName(date, timeZone){
        return moment.tz(date, timeZone).format('zz'); //EDT or EST
    }

}