import { Component } from '@angular/core';
import { AppService } from './app.service';

import { Observable } from 'rxjs/Observable';
import { FormControl } from '@angular/forms';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/map';

import { configs } from './configs';


import { HttpClient } from '@angular/common/http';
import { forkJoin } from "rxjs/observable/forkJoin";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'app';
  symbol : FormControl = new FormControl();
  // searchResult: Observable<any[]>;
  symbolName = "AAPL";
  searchResult;

  priceChartOptions: Object;
  SMAChartOptions: Object;
  EMAChartOptions: Object;

  arr = ["SMA", "EMA", "STOCH"];

  constructor(private service: AppService, private http: HttpClient){ 
    this.symbol.valueChanges
    .debounceTime(150)
    .subscribe(data => {
        if(data !== ""){
          this.service.searchSymbol(data).subscribe(response =>{
              this.symbolName = data;
              console.log(this.symbolName);
              this.searchResult = response;
              console.log(response);
          })
        }else{
          this.searchResult = [];
        }
    })
  }


  charts = [];

  ngOnInit(){ 
    this.onSubmit('AAPL');

    this.drawSingleLineChart('AAPL');
  }


  parseSingleTarget(jsonObj, target){
      var array_date = [];
      var array_indicator = [];
      var cnt = 0;
      for(var key in jsonObj) {
          if(cnt++ >= 126)
              break;
          array_date.push(key.substring(5).replace(/-/g, "\/"));
          array_indicator.push(parseFloat(jsonObj[key][target]));
      }
      array_date.reverse();
      array_indicator.reverse();
      return {
        date: array_date,
        indicator: array_indicator
      };
  }

  
  async drawSingleLineChart(symbol){
    // let SMAURL = this.http.get('http://localhost:12345/?type=indicator&symbol=AAPL&indicator=SMA');
    // let EMAURL = this.http.get('http://localhost:12345/?type=indicator&symbol=AAPL&indicator=EMA');
    // let STOCHURL = this.http.get('http://localhost:12345/?type=indicator&symbol=AAPL&indicator=STOCH');
    // var dataSMA, dataEMA, dataSTOCH;
    // forkJoin([SMAURL, EMAURL, STOCHURL]).subscribe(results => {
    //   dataSMA = results[0]
    //   console.log(dataSMA);
    //   dataEMA = results[1];
    //   console.log(dataEMA);
    //   dataSTOCH = results[2];
    //   console.log(dataSTOCH);
    // });

    /***** SMA *****/
    this.http.get('http://localhost:12345/?type=indicator&symbol=AAPL&indicator=SMA').subscribe(dataSMA => {
      console.log(dataSMA);
      var SMA_data = dataSMA['Technical Analysis: SMA']; //full size data
      var parseRes = this.parseSingleTarget(SMA_data, 'SMA');
      // configs[0]['title']['text'] = 'SMA';
      configs[0]['xAxis']['categories'] = parseRes.date;
      configs[0]['series'][0]['data'] = parseRes.indicator;
      this.SMAChartOptions = configs[0];
    });
    /***** SMA *****/


    /***** EMA *****/
    this.http.get('http://localhost:12345/?type=indicator&symbol=AAPL&indicator=EMA').subscribe(dataEMA => {
      console.log(dataEMA);
      var EMA_data = dataEMA['Technical Analysis: EMA']; //full size data
      var parseRes = this.parseSingleTarget(EMA_data, 'EMA');
      configs[1]['xAxis']['categories'] = parseRes.date;
      configs[1]['series'][0]['data'] = parseRes.indicator;
      this.EMAChartOptions = configs[1];
    });
    /***** EMA *****/

    // configs[0]['title']['text'] = 'EMA';
    // configs[0]['xAxis']['categories'] = parseRes.date;
    // configs[0]['series'][0]['data'] = parseRes.indicator;
    // console.log('EMA: ' + configs[0]['series'][0]['data']);
    // this.EMAChartOptions = configs[0];

  }




  /**
   * query:
   * (1) price & volume data
   * (2) all indicators 
   * @param value 
   */
  async onSubmit(value) {
    console.log(value);
    var data = await this.service.queryPrice(value);
    console.log("service: " + data.json()['Meta Data']['1. Information']);

    var json_series_data = data.json()['Time Series (Daily)']; 
    var array_date = [];
    var array_price = [];
    var array_volume = [];
    var max_volume = 0;

    var cnt = 0;
    for(var key in json_series_data) {
        if(cnt >= 126)
            break;
        array_date.push(key.substring(5).replace(/-/g, "\/"));
        array_price.push(parseFloat(json_series_data[key]['4. close']));

        var volume = parseFloat(json_series_data[key]['5. volume']);
        array_volume.push(volume);
        max_volume = Math.max(max_volume, volume);
        cnt++;
    }

    max_volume *= 1.5;
    //remember to reverse
    array_date.reverse();
    array_price.reverse();
    array_volume.reverse();

    this.priceChartOptions = {
      chart: {
        zoomType: 'x',
        width: null
      },
      title: {
          text: value + ' Stock Price and Volume'
      },
      subtitle: {
          useHTML:true,
          text:"<a style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
      },
      xAxis: {
        categories: array_date,
        tickPositioner: function() {
            let res = [];
            for(let i = 0; i < this.categories.length; i++) {
                if(i % 10 == 0) 
                    res.push(this.categories.length - 1 - i);
            }
            return res;
        }
      },
      yAxis: [
        {
          title: {
            text: 'Stock Price'
          },
          labels:{
            format:'{value:,.2f}'
          },
        },
        {
          title:{
            text:'Volume'
          },
          opposite:true,
          max: max_volume
        }
      ],
      plotOptions: {
        area: {
          lineWidth: 2,
          states: {
            hover: {
              lineWidth: 2
            }
          }
        }
      },
      series: [
        {
          type: 'area',
          name: 'Pirce',
          data: array_price, //data
          yAxis:0,
          tooltip:{
            pointFormat: value + ': {point.y:,..2f}'
          },
          marker:{
              enabled:false
          },
        },
        {
          type: 'column',
          name: 'Volume',
          data: array_volume,
          yAxis:1,
          color: 'red'
        }
      ]
    };
  }

}
