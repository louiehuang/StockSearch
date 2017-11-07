import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class ChartsService {
    url: string

    constructor(private http : Http){
        this.url  = 'stockDetails/test.json'
    }

    searchSymbol(symbol){
        console.log(this.url + symbol);
        return this.http.get(this.url + symbol).map(res => {
            return res.json().map(item => {
                return item;
            })
        })
    }
}