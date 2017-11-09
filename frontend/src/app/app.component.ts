import { Component } from '@angular/core';
import { AppService } from './app.service';
import { ChartsService } from './charts.service';

import { Observable } from 'rxjs/Observable';
import { FormControl } from '@angular/forms';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/map';

import { configs } from './configs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { trigger, state, style, transition, animate, keyframes } from '@angular/animations';

import { FacebookService, InitParams, UIParams, UIResponse } from 'ngx-facebook';

export class Stock {
  //addTime is the timestamp that a user add a stock to his favorite list
  constructor(public symbol: string, public price: string, public change: number, 
    public changePercent: number, public volume: number, public addTime: number) {}
} 

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  animations: [
    trigger('toStockChart', [
      state('in', style({transform: 'translateX(0)'})),
      transition('* => in', [
        style({transform: 'translateX(100%)'}),
        animate(500)
      ]),
    ]),
    trigger('toFavoriteList', [
      state('in', style({transform: 'translateX(0)'})),
      transition('* => in', [
        style({transform: 'translateX(-100%)'}),
        animate(500)
      ])
    ]),
  ]
})
export class AppComponent {
  title = 'Stock Search';

  state: string;
  inFavoriteList: boolean = true;
  switchDivAnimate(){
    // if (this.state === 'favList') {
    //   this.state = 'in';
    // }
    this.state = 'in';
    this.inFavoriteList = !this.inFavoriteList;
  }

  favoriteList: Stock[]; //fetch from local storage

  orderKey = 'price';
  orderRule = false; //reverse 'false' => ascending order, true for descending

  symbol : FormControl = new FormControl();
  // searchResult: Observable<any[]>;
  searchSymbolName = "AAPL"; //this value will change when typing search input
  symbolName = "AAPL"; //this value don't change when typing search input
  searchResult: Object;

  priceJson: Object;

  lastPrice; changeNum; changePercent; changeToColor; changeToImg;
  timestamp; curOpen; curClose; curRange; curVolume; timeZone;

  //chart
  priceChartOptions: Object;
  SMAChartOptions: Object; EMAChartOptions: Object;
  RSIChartOptions: Object; ADXChartOptions: Object; CCIChartOptions: Object;
  STOCHChartOptions: Object; BBANDSChartOptions: Object; MACDChartOptions: Object;

  indexMap = {'Price': 0, 'SMA': 1, 'EMA': 2, 'STOCH': 3, 'RSI': 4,
              'ADX': 5, 'CCI': 6, 'BBANDS': 7, 'MACD': 8};
  chartIdentity; //show which chart the user is currently looking, 'Pirce', 'SMA'...

  //stock
  stockChartOptions: Object;

  //news
  newsArray = [];

  constructor(private service: AppService, private chartService: ChartsService, private fb: FacebookService,
              private http: HttpClient){ 
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
    });

    let initParams: InitParams = {
      appId: '918643571616008',
      xfbml: true,
      version: 'v2.11'
    };
    fb.init(initParams);

  }


  ngOnInit(){ 
    //drawLineCharts and draw stock chart in onSubmit()
    this.onSubmit('AAPL'); //test

    //time use timestamp
    this.favoriteList = [new Stock('AAPL', '153.28', -0.95, -0.62, 21896592, 1510204242065),
        new Stock('MSFT', '73.28', 0.03, -0.62, 11896592, 1510204245065),
        new Stock('YHOO', '53.28', 0.15, 0.62, 11896592, 1510204145065)];

    localStorage.setItem('favoriteList', JSON.stringify(this.favoriteList));
  }


  // ngAfterViewInit(){
  //   //if stock user is searching is in his favorite list, change icon
  //   var favoriteCheckRes = this.isStockInFavoriteList();
  //   if(favoriteCheckRes.found){
  //     document.getElementById("btn_fav").className = "glyphicon glyphicon-star";
  //   }
  // }

  /**
   * sorting key changes, eg. sort by symbol, price, volume...
   * @param value 
   */
  onSortingKeyChange(value){
    this.orderKey = value;
    // console.log(this.orderKey);
  }

  /**
   * sorting rule changes, eg. asc or desc
   * @param value 
   */
  onSortingRuleChange(value){
    if(value === "ascending"){
      this.orderRule = false;
    }else if(value === "descending"){
      this.orderRule = true;
    }
    // console.log(this.orderRule);
  }

  /**
   * add (or remove) stock to favorite list
   */
  addToFavorite(){
    var favoriteCheckRes = this.isStockInFavoriteList();
    
    if(favoriteCheckRes.found){
      //if in favorite List, remove it change ang img to empty-star
      document.getElementById("btn_fav").className = "glyphicon glyphicon-star-empty";
      this.removeCurrentStockToFavoriteList(favoriteCheckRes.index);
    }else{
      //else add to favorite List, and change img to star
      document.getElementById("btn_fav").className = "glyphicon glyphicon-star";
      this.addCurrentStockToFavoriteList();
    }

    localStorage.setItem('favoriteList', JSON.stringify(this.favoriteList));
  }

  /**
   * check whether current searching stock is in user's favorite list
   * if so, return true and its index; otherwise, return false and -1
   */
  isStockInFavoriteList(){
    this.favoriteList = JSON.parse(localStorage.getItem('favoriteList'));
    //console.log(this.favoriteList); 
    var found = false, index = -1;
    for(var i = 0; i < this.favoriteList.length; i++) {
        if (this.favoriteList[i].symbol == this.symbolName) {
            found = true;
            index = i;
            break;
        }
    }
    return {found, index};
  }

  /**
   * remove stock at index
   * @param index 
   */
  removeCurrentStockToFavoriteList(index){
    this.favoriteList.splice(index, 1);
    console.log(this.favoriteList);
  }

  /**
   * add current stock to favorite list
   */
  addCurrentStockToFavoriteList(){
    var addTime = Date.now();
    console.log(addTime);
    this.favoriteList.push(
      new Stock(this.symbolName, this.lastPrice, this.changeNum, 
        this.changePercent, this.curVolume, addTime));
    console.log(this.favoriteList);
  }


  /**
   * change chart Id when clicking different chart (price and indicators chart)
   * @param value 
   */
  updateChartId(value){
    this.chartIdentity = value;
    console.log(this.chartIdentity);
    console.log(this.indexMap[this.chartIdentity]);
  }

  /**
   * export stock chart to facebook
   */
  shareOnFacebook(){
    //export chart
    let chartOptions = JSON.stringify(this.priceChartOptions);
    switch(this.indexMap[this.chartIdentity]){
      case 1:{ chartOptions = JSON.stringify(this.SMAChartOptions); break; } 
      case 2:{ chartOptions = JSON.stringify(this.EMAChartOptions); break; }
      case 3:{ chartOptions = JSON.stringify(this.STOCHChartOptions); break; }
      case 4:{ chartOptions = JSON.stringify(this.RSIChartOptions); break; }
      case 5:{ chartOptions = JSON.stringify(this.ADXChartOptions); break; }
      case 6:{ chartOptions = JSON.stringify(this.CCIChartOptions); break; }
      case 7:{ chartOptions = JSON.stringify(this.BBANDSChartOptions); break; }
      case 8:{ chartOptions = JSON.stringify(this.MACDChartOptions); break; }
    }
    let imgURL = encodeURI('async=false&type=jpeg&width=600&options=' + chartOptions);
    let exportImgURL = 'http://export.highcharts.com/';

    this.http.post(exportImgURL, imgURL, {
      responseType: 'text',
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8')
                .append('accept', '*/*')
    }).subscribe(res => {
      console.log(exportImgURL + res);
      let params: UIParams = {
        method: 'feed',
        link: exportImgURL + res  //Full URL
      }
      this.fb.ui(params)
        .then((res: UIResponse) => {
          alert('Shared Successfully');
          console.log(res);
        }).catch((e: any) => {
          alert('Canceled');
          // console.error(e);
        });
    },
      err =>{
        // console.log(err);
    });
  }

  /**
   * Draw all indicator charts
   * @param symbol 
   */
  drawLineCharts(symbol){
    console.log("draw line: " + symbol);
    
    /***** Single Line *****/
    this.drawSingleLineChart(this.indexMap, symbol, 'SMA');
    this.drawSingleLineChart(this.indexMap, symbol, 'EMA');
    this.drawSingleLineChart(this.indexMap, symbol, 'RSI');
    this.drawSingleLineChart(this.indexMap, symbol, 'ADX');
    this.drawSingleLineChart(this.indexMap, symbol, 'CCI');
    /***** Single Line*****/

    /***** Mutiple Lines *****/
    this.drawMultipleLineChart(this.indexMap, symbol, 'STOCH', 'SlowD', 'SlowK', ''); //Two
    this.drawMultipleLineChart(this.indexMap, symbol, 'BBANDS', 'Real Middle Band', 'Real Lower Band', 'Real Upper Band'); //Three
    this.drawMultipleLineChart(this.indexMap, symbol, 'MACD', 'MACD_Signal', 'MACD', 'MACD_Hist'); //Three
    /***** Mutiple Lines *****/
  }


  /**
   * query:
   * (1) price & volume data
   * (2) all indicators 
   * @param value 
   */
  async onSubmit(value) {
    // var data = await this.service.queryPrice(value);
    var baseURL = 'http://localhost:12345/?type=price&symbol=';
    console.log("onSubmit: " + baseURL + value);

    this.http.get(baseURL + value).subscribe(data => {
      var meta_data = data['Meta Data']; 
      var json_series_data = data['Time Series (Daily)']; 
      var parseRes = this.chartService.parsePriceData(json_series_data);
  
      //update table
      this.symbolName = value;
      console.log("onSubmit: " + this.symbolName);

      this.timeZone = meta_data['5. Time Zone'];
      console.log(this.timeZone);

      //YYYY-MM-DD when closed or YYYY-MM-DD HH:mm:ss when open
      var lastRefreshedTime = meta_data['3. Last Refreshed'].toString();
      if(lastRefreshedTime.length <= 12) //"2017-11-07"
        lastRefreshedTime += " 16:00:00";
      
      this.timestamp = lastRefreshedTime + " " + this.chartService.getTimeZoneName(lastRefreshedTime, this.timeZone);
        
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
        chart: { zoomType: 'x', width: null },
        title: { text: value + ' Stock Price and Volume' },
        subtitle: {
            useHTML:true,
            text:"<a target='_blank' style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
        },
        xAxis: {
          categories: parseRes.date,
          tickPositioner: function() {
              let res = [];
              for(let i = 0; i < this.categories.length; i++)
                  if(i % 10 == 0) 
                      res.push(this.categories.length - 1 - i);
              return res;
          }
        },
        yAxis: [
          {
            title: { text: 'Stock Price' },
            labels:{ format:'{value:,.2f}' },
          },
          {
            title:{ text:'Volume' },
            opposite:true,
            max: parseRes.max_volume
          }
        ],
        plotOptions: {
          area: { lineWidth: 2, states: { hover: { lineWidth: 2 } } }
        },
        series: [
          {
            type: 'area',
            name: 'Pirce',
            data: parseRes.price, //data
            yAxis:0,
            tooltip:{ pointFormat: value + ': {point.y:,..2f}' },
            marker:{ enabled:false },
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

      this.createStockChart(json_series_data);
      //have to put createNewArray inside this https.get() to get timeZone
      //If put outsize this function, this.timeZone is undefined...
      this.createNewArray(value, this.timeZone);
    });


    //The order is this.http.get() and drawLineCharts() run simultaneously
    //after onSubmit()'s code finished, then createStockChart() and createNewArray()
    this.drawLineCharts(value);
  }

  createNewArray(symbol, timeZone){
    var baseURL = 'http://localhost:12345/?type=news&symbol=';
    console.log("createNewArray: " + baseURL + symbol);

    this.http.get(baseURL + symbol).subscribe(data => {
      //parse, get 5 news and convert news link if needed
      var limit = 5;
      this.newsArray = this.chartService.parseNew(data, timeZone, limit); //jsonObj

      console.log(this.newsArray);
    });

  }





  /**
   * draw historical stock chart
   * @param data 
   */
  createStockChart(data){
    //[[1383202800000, 35.405], [1383289200000, 35.525], ... [1508396400000, 77.91]]
    //1000 elements
    var parseRes = this.chartService.parseStockData(data);

    this.stockChartOptions = {
      chart: {
        height: 400, width: null
    },
    title: {
        text: this.symbolName + ' Stock Value'
    },
    subtitle: {
        useHTML:true,
        text: "<a target='_blank' style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
    },
    rangeSelector: {
        selected: 1
    },
    series: [{
        name: this.symbolName + ' Stock Price',
        data: parseRes,
        type: 'area',
        threshold: null,
        tooltip: {
            valueDecimals: 2
        }
    }],
    responsive: {
        rules: [{
            condition: {
                maxWidth: 500
            },
            chartOptions: {
                chart: {
                    height: 300
                },
                subtitle: {
                    text: null
                },
                navigator: {
                    enabled: false
                }
            }
        }]
      }
    }
  }

  /**
   * drwa a single line chart
   * @param indexMap 
   * @param symbol 
   * @param indicator 
   */
  drawSingleLineChart(indexMap, symbol, indicator){
    var baseURL = "http://localhost:12345/?type=indicator&symbol=" + symbol;
    console.log("drawSingleLineChart: " + baseURL + '&indicator=' + indicator);
    this.http.get(baseURL + '&indicator=' + indicator).subscribe(data => {
      console.log(data);
      var indicator_data = data['Technical Analysis: ' + indicator]; //full size data
      var parseRes = this.chartService.parseSingleTarget(indicator_data, indicator);
      var singleLineCharOption = {
        chart: { zoomType: 'x' },
        title: { text: ''  },
        subtitle: {
            useHTML:true,
            text: "<a target='_blank' style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
        },
        xAxis: {
            categories: [],
            tickPositioner: function() {
                let res = [];
                for(let i = 0; i < this.categories.length; i++) 
                    if(i % 5 == 0) 
                        res.push(this.categories.length - 1 - i);
                return res;
            }
        },
        yAxis: {
            title: { text: '' }
        },
        plotOptions: {
            states: { hover: { lineWidth: 1 } }
        },
        series: [{
            name: '',
            lineWidth: 1,
            marker: { enabled: true, symbol:'square', radius: 2 },
            data: []
        }]
      };

      singleLineCharOption['title']['text'] = indicator;
      singleLineCharOption['yAxis']['title']['text'] = indicator;
      singleLineCharOption['xAxis']['categories'] = parseRes.date;
      singleLineCharOption['series'][0]['name'] = symbol + ' ' + indicator;
      singleLineCharOption['series'][0]['data'] = parseRes.indicator;

      switch(indexMap[indicator]){
        case 1:{ this.SMAChartOptions = singleLineCharOption; break; } 
        case 2:{ this.EMAChartOptions = singleLineCharOption; break; }
        case 4:{ this.RSIChartOptions = singleLineCharOption; break; }
        case 5:{ this.ADXChartOptions = singleLineCharOption; break; }
        case 6:{ this.CCIChartOptions = singleLineCharOption; break; }
      }

    });
  }

  
  /**
   * draw indicator chart with mutiple target
   * @param indexMap 
   * @param symbol 
   * @param indicator 
   * @param target1 
   * @param target2 
   * @param target3 
   */
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

      var mutipleLineChartOption = {
        chart: { zoomType: 'x' },
        title: { text: '' },
        subtitle: {
            useHTML:true,
            text: "<a target='_blank' style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
        },
        xAxis: {
            categories: [],
            tickPositioner: function() {
                let res = [];
                for(let i = 0; i < this.categories.length; i++)
                    if(i % 5 == 0) 
                        res.push(this.categories.length - 1 - i);
                return res;
            }
        },
        yAxis: {
            title: { text: '' },
            marker:{ enabled: true, symbol:'square', radius: 1, },
        },
        tooltip: {
            crosshairs: true,
            shared: true
        },
        series: [{
            name: '', data: [], lineWidth: 1,
            marker: { enabled: true, symbol:'square', radius: 2 }
        },
        {
            name: '', data: [], lineWidth: 1,
            marker: { enabled: true, symbol:'square', radius: 2 }
        }]
      };

      mutipleLineChartOption['title']['text'] = indicator;
      mutipleLineChartOption['xAxis']['categories'] = parseRes.date;
      mutipleLineChartOption['series'][0]['name'] = symbol + ' ' + target1;
      mutipleLineChartOption['series'][0]['data'] = parseRes.indicator_1;
      mutipleLineChartOption['series'][1]['name'] = symbol + ' ' + target2;
      mutipleLineChartOption['series'][1]['data'] = parseRes.indicator_2;

      if(isTwoLine == false){ //three lines
        //add a new series
        var newSeries = {'name':'', 'data':[], 'lineWidth': 1, 'marker': { enabled: true, symbol:'square', radius: 2 }};
        newSeries['name'] = symbol + ' ' + target3;
        newSeries['data'] = parseRes.indicator_3;
        mutipleLineChartOption['series'].push(newSeries);
      }

      switch(indexMap[indicator]){
        case 3:{ this.STOCHChartOptions = mutipleLineChartOption; break; } 
        case 7:{ this.BBANDSChartOptions = mutipleLineChartOption; break; }
        case 8:{ this.MACDChartOptions = mutipleLineChartOption; break; }
      }      
    });
  }
  

}
