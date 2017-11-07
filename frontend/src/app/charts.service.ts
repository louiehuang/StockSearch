import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class ChartsService {
    constructor(private http : Http){
        
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
  


}