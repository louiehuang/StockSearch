import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class AppService {
    ac_url: string;
    stock_url: string;
    indicator_url: string;

    constructor(private http : Http){
        // this.url  = 'http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input='
        this.ac_url  = 'http://liuyinstock.us-east-2.elasticbeanstalk.com/autocomplete?symbol=';
        this.stock_url = 'http://liuyinstock.us-east-2.elasticbeanstalk.com/price?symbol=';
        this.indicator_url  = 'http://liuyinstock.us-east-2.elasticbeanstalk.com/indicator?';
    }

    /**
     * used for auto complete
     * @param symbol 
     */
    searchSymbol(symbol){
        let fullURL = this.ac_url + symbol;
        console.log(fullURL);
        return this.http.get(fullURL).map(res => {
            return res.json().map(item => {
                return item;
            })
        })
    }

    /**
     * query price and volume of the specific symbol
     * @param symbol 
     */
    queryPrice(symbol){
        let fullURL = this.stock_url + symbol;
        console.log(fullURL);
        return this.http.get(fullURL).toPromise();
    }
}