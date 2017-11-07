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
        this.ac_url  = 'http://localhost:12345/?type=autocomplete&symbol=';
        this.stock_url  = 'http://localhost:12345/?type=price&symbol=';
        this.indicator_url  = 'http://localhost:12345/?type=indicator';
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

    
    
    // /**
    //  * query a single indicator of the specific symbol
    //  * http://localhost:12345/?type=indicator&symbol=AAPL&indicator=SMA
    //  * @param symbol 
    //  * @param indicator 
    //  */
    // queryIndicator(symbol, indicator){
    //     let fullURL = this.indicator_url + '&symbol=' + symbol + "&indicator=" + indicator;
    //     console.log(fullURL);
    //     return this.http.get(fullURL).toPromise();
    // }



    // /**
    //  * query all indicators of the specific symbol
    //  * @param symbol 
    //  */
    // queryAllIndicators(symbol){
    //     let fullURL = this.indicator_url + '&symbol=' + symbol;
    //     console.log(fullURL);
    //     return this.http.get(fullURL).toPromise();
    // }

}