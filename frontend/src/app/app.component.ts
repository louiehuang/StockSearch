import { Component } from '@angular/core';
import { AppService } from './app.service';
import { ChartsService } from './charts.service';

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
  title = 'Stock Search';
  symbol : FormControl = new FormControl();
  // searchResult: Observable<any[]>;
  searchSymbolName = "AAPL"; //this value will change when typing search input
  symbolName = "AAPL"; //this value don't change when typing search input
  searchResult: Object;

  lastPrice; changeNum; changePercent; changeToColor; changeToImg;
  timestamp; curOpen; curClose; curRange; curVolume; 

  priceChartOptions: Object;
  SMAChartOptions: Object; EMAChartOptions: Object;
  RSIChartOptions: Object; ADXChartOptions: Object; CCIChartOptions: Object;
  STOCHChartOptions: Object; BBANDSChartOptions: Object; MACDChartOptions: Object;

  constructor(private service: AppService, private chartService: ChartsService, private http: HttpClient){ 
    this.symbol.valueChanges
    .debounceTime(150)
    .subscribe(data => {
        if(data !== ""){
          this.service.searchSymbol(data).subscribe(response =>{
              this.searchSymbolName = data;
              console.log(this.searchSymbolName);
              this.searchResult = response;
              console.log(response);
          })
        }else{
          this.searchResult = [];
        }
    })
  }


  ngOnInit(){ 
    this.onSubmit('AAPL');
    this.drawLineCharts('AAPL');
  }




  /**
   * Draw all indicator charts
   * @param symbol 
   */
  drawLineCharts(symbol){
    var indexMap = [];
    indexMap['Price'] = 0; //for price chart
    indexMap['SMA'] = 1; indexMap['EMA'] = 2; indexMap['STOCH'] = 3; indexMap['RSI'] = 4;
    indexMap['ADX'] = 5; indexMap['CCI'] = 6; indexMap['BBANDS'] = 7; indexMap['MACD'] = 8;
    
    /***** Single Line *****/
    this.drawSingleLineChart(indexMap, symbol, 'SMA');
    this.drawSingleLineChart(indexMap, symbol, 'EMA');
    this.drawSingleLineChart(indexMap, symbol, 'RSI');
    this.drawSingleLineChart(indexMap, symbol, 'ADX');
    this.drawSingleLineChart(indexMap, symbol, 'CCI');
    /***** Single Line*****/

    /***** Mutiple Lines *****/
    this.drawMultipleLineChart(indexMap, symbol, 'STOCH', 'SlowD', 'SlowK', ''); //Two
    this.drawMultipleLineChart(indexMap, symbol, 'BBANDS', 'Real Middle Band', 'Real Lower Band', 'Real Upper Band'); //Three
    this.drawMultipleLineChart(indexMap, symbol, 'MACD', 'MACD_Signal', 'MACD', 'MACD_Hist'); //Three
    /***** Mutiple Lines *****/
  }

  /**
   * drwa a single line chart
   * @param indexMap 
   * @param symbol 
   * @param indicator 
   */
  drawSingleLineChart(indexMap, symbol, indicator){
    var baseURL = "http://localhost:12345/?type=indicator&symbol=" + symbol;
    this.http.get(baseURL + '&indicator=' + indicator).subscribe(data => {
      console.log(data);
      var indicator_data = data['Technical Analysis: ' + indicator]; //full size data
      var parseRes = this.chartService.parseSingleTarget(indicator_data, indicator);

      configs[indexMap[indicator]]['xAxis']['categories'] = parseRes.date;
      configs[indexMap[indicator]]['series'][0]['name'] = symbol + ' ' + indicator;
      configs[indexMap[indicator]]['series'][0]['data'] = parseRes.indicator;
      switch(indexMap[indicator]){
        case 1:{ this.SMAChartOptions = configs[indexMap[indicator]]; break; } 
        case 2:{ this.EMAChartOptions = configs[indexMap[indicator]]; break; }
        case 4:{ this.RSIChartOptions = configs[indexMap[indicator]]; break; }
        case 5:{ this.ADXChartOptions = configs[indexMap[indicator]]; break; }
        case 6:{ this.CCIChartOptions = configs[indexMap[indicator]]; break; }
      }
    });
  }


  /**
   * draw chart with two lines (two targets)
   * @param indexMap 
   * @param symbol 
   * @param indicator 
   * @param target1 
   * @param target2 
   */
  drawTwoLineChart(indexMap, symbol, indicator, target1, target2){
    var baseURL = "http://localhost:12345/?type=indicator&symbol=" + symbol;
    this.http.get(baseURL + '&indicator=' + indicator).subscribe(data => {
      console.log(data);
      var indicator_data = data['Technical Analysis: ' + indicator]; //full size data
      var parseRes = this.chartService.parseTwoTarget(indicator_data, target1, target2); //SlowD, SlowK

      configs[indexMap[indicator]]['xAxis']['categories'] = parseRes.date;
      configs[indexMap[indicator]]['series'][0]['name'] = symbol + ' ' + target1;
      configs[indexMap[indicator]]['series'][0]['data'] = parseRes.indicator_1;
      configs[indexMap[indicator]]['series'][1]['name'] = symbol + ' ' + target2;
      configs[indexMap[indicator]]['series'][1]['data'] = parseRes.indicator_2;
      this.STOCHChartOptions = configs[indexMap[indicator]];
    });
  }

  drawMultipleLineChart(indexMap, symbol, indicator, target1, target2, target3){
    var baseURL = "http://localhost:12345/?type=indicator&symbol=" + symbol;
    var isTwoLine = false;
    if(target3.length == 0)
      isTwoLine = true;

    this.http.get(baseURL + '&indicator=' + indicator).subscribe(data => {
      console.log(data);
      var indicator_data = data['Technical Analysis: ' + indicator]; //full size data
      var parseRes;
      if(isTwoLine == true)
        parseRes = this.chartService.parseTwoTarget(indicator_data, target1, target2); //SlowD, SlowK
      else 
        parseRes = this.chartService.parseThreearget(indicator_data, target1, target2, target3); 

      configs[indexMap[indicator]]['xAxis']['categories'] = parseRes.date;
      configs[indexMap[indicator]]['series'][0]['name'] = symbol + ' ' + target1;
      configs[indexMap[indicator]]['series'][0]['data'] = parseRes.indicator_1;
      configs[indexMap[indicator]]['series'][1]['name'] = symbol + ' ' + target2;
      configs[indexMap[indicator]]['series'][1]['data'] = parseRes.indicator_2;

      if(isTwoLine == false){
        //three lines
        configs[indexMap[indicator]]['series'][2]['name'] = symbol + ' ' + target3;
        configs[indexMap[indicator]]['series'][2]['data'] = parseRes.indicator_3;
      }

      switch(indexMap[indicator]){
        case 3:{ this.STOCHChartOptions = configs[indexMap[indicator]]; break; } 
        case 7:{ this.BBANDSChartOptions = configs[indexMap[indicator]]; break; }
        case 8:{ this.MACDChartOptions = configs[indexMap[indicator]]; break; }
      }      
    });
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

    var meta_data = data.json()['Meta Data']; 
    var json_series_data = data.json()['Time Series (Daily)']; 
    var parseRes = this.chartService.parsePriceData(json_series_data);

    //update table
    this.symbolName = meta_data['2. Symbol'];
    var timeZone = meta_data['5. Time Zone'];

    // console.log(Object.keys(json_series_data)[0]);
    //cur day (key) is Object.keys(json_series_data)[0]
    var curObj = json_series_data[Object.keys(json_series_data)[0]]; //current day
    var prevObj = json_series_data[Object.keys(json_series_data)[1]]; //previous day
    this.curOpen = parseFloat(curObj['1. open']).toFixed(2);
    this.curClose = parseFloat(curObj['4. close']).toFixed(2);
    this.curRange = parseFloat(curObj['3. low']).toFixed(2) + ' - ' + parseFloat(curObj['2. high']).toFixed(2);
    this.curVolume = curObj['5. volume'].toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");;

    this.lastPrice = parseFloat(prevObj['4. close']).toFixed(2);
    
    this.changeNum = (this.curClose - this.lastPrice).toFixed(2);
    // console.log(((this.changeNum / this.lastPrice) * 100).toFixed(2));

    this.changePercent = ((this.changeNum / this.lastPrice) * 100).toFixed(2) + "%";
    // this.changeNum = -0.2; //test negative change

    //draw price chart
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
        categories: parseRes.date,
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
          max: parseRes.max_volume
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
          data: parseRes.price, //data
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
          data: parseRes.volume,
          yAxis:1,
          color: 'red'
        }
      ]
    };
  }
}
