import { Component, ChangeDetectorRef } from '@angular/core';
import { AppService } from './app.service';
import { ChartsService } from './charts.service';
import { Observable, Subscription } from 'rxjs/Rx';
import { FormControl } from '@angular/forms';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/map';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { trigger, state, style, transition, animate, keyframes } from '@angular/animations';
import { FacebookService, InitParams, UIParams, UIResponse } from 'ngx-facebook';
import { BootstrapSwitchComponent } from 'angular2-bootstrap-switch';

declare let $: any;

/**
 * This class is used for building favorite list
 */
export class Stock {
  //addTime is the timestamp that a user add a stock to his favorite list
  constructor(public symbol: string, public price: number, public change: number, 
    public changePercent: number, public volume: string, public addTime: number) {}
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
  serverURL: string = "http://liuyinstock.us-east-2.elasticbeanstalk.com";

  //switch button params
  onText='ON'; offText='OFF'; onColor="blue"; offColor="gray"; switchSize="small";

  //input is valid (not empty and not only space)
  isInvalidInput: boolean = false; //Input Border
  validQuote: boolean = false; //Get Quete Button
  //when chart has no data, cannot press btn-goStock
  existStockInfo: boolean = false; //false diable btn-goStock ('>') button

  state: string; //animation state
  //current page is at favorite list
  isInFavoriteListPage: boolean = true;
  switchDivAnimate(){
    // if (this.state === 'favList') { this.state = 'in'; }
    this.state = 'in';
    this.isInFavoriteListPage = !this.isInFavoriteListPage;
    //change sorting rule to default
    this.orderKey = 'default';
    this.orderRule = false; //reverse 'false' => ascending order, true for descending
  }

  //favorite list and sorting
  currentStockInFavoriteList: boolean = false;
  favoriteList: Stock[]; //fetch from local storage
  orderKey = 'default';
  orderRule = false; //reverse 'false' => ascending order, true for descending

  //auto refresh
  autoRefresh: boolean = false;
  timer; //refresh timer
  subscript: Subscription;

  //record whether data is still been querying, used for process bar
  //false means data has not been received yet, still being querting (processing)
  loadingMap = {'Price': false, 'SMA': false, 'EMA': false, 'STOCH': false, 'RSI': false,
  'ADX': false, 'CCI': false, 'BBANDS': false, 'MACD': false, 'Table': false, 'HighStock': false, 'News': false};

  //record whetehr error occurs in data querying, used for alert (error handling)
  errInloadingMap = {'Price': false, 'SMA': false, 'EMA': false, 'STOCH': false, 'RSI': false,
  'ADX': false, 'CCI': false, 'BBANDS': false, 'MACD': false, 'News': false};

  symbol : FormControl = new FormControl();
  searchSymbolName = ""; //this value will change when typing search input
  symbolName = ""; //this value don't change when typing search input
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
        private http: HttpClient, private ref: ChangeDetectorRef){ 
    this.symbol.valueChanges
    .debounceTime(50)
    .subscribe(data => {
      this.searchSymbolName = data; //input value
      if(this.inputValidationCheck(data)){ //input is valid
        document.getElementById("input_symbol").className = "";
        this.isInvalidInput = false;
        this.validQuote = true;
        this.ref.detectChanges(); //update UI manually
        this.service.searchSymbol(data).subscribe(response =>{
            this.searchResult = response;
        })
      }else{ //invalid input value
        this.isInvalidInput = true; //input box's border turns red and show error message
        this.validQuote = false; //disable 'Get Quote' button
        document.getElementById("input_symbol").className = "input_error";
        this.searchResult = []; //set to empty
      }
    });

    //face book API
    let initParams: InitParams = {
      appId: '918643571616008', //my api id
      xfbml: true,
      version: 'v2.11'
    };
    fb.init(initParams);
  }


  ngOnInit(){ 
    ////test favoriteList, time use timestamp
    this.favoriteList = [new Stock('AAPL', 153.28, -0.95, -0.62, '21,896,592', 1510204242065),
        new Stock('MSFT', 73.28, 0.03, -0.62, '11,896,592', 1510204245065),
        new Stock('YHOO', 53.28, 0.15, 0.62, '11,896,592', 1510204145065)];
    localStorage.setItem('favoriteList', JSON.stringify(this.favoriteList));
  }

  /**
   * switch button change (turn on or off automatic refresh)
   * @param event 
   */
  onFlagChange(event){
    console.log(event); //true or false
    this.autoRefresh = (this.autoRefresh === true ? false : true);
    if(this.autoRefresh){
      console.log('start auto refreshing');
      this.timer = Observable.timer(0, 5000); //delay, period
      this.subscript = this.timer.subscribe(data=> {
        this.updateFavoriteList();
      });
    }else{
      console.log('stop auto refreshing');
      this.subscript.unsubscribe();
    }
  }

  /**
   * clear input and change to first page (Favorite List Page)
   */
  clear(){
    this.isInFavoriteListPage = true; //change to first page (Favorite List Page)
    this.existStockInfo = false;
    this.validQuote = false;
  }

  /**
   * symbol input box on blur, if valid ,change box border to res and show error message
   */
  onInputBlur(){
    // console.log(this.searchSymbolName);
    let valid = this.inputValidationCheck(this.searchSymbolName);
    if(!valid){
      document.getElementById("input_symbol").className = "input_error";
    }else{
      document.getElementById("input_symbol").className = "";
    }
    console.log("input valid: " + valid);
  }

  /**
   * auto refresh calls this function, iterate all favorite symbol and query new data
   */
  updateFavoriteList(){
    console.log("refresh function begins");
    if(this.favoriteList === null || this.favoriteList === undefined || this.favoriteList.length === 0){
      console.log('favoriteList is empty, do not refresh');
      return;
    }
    for (let i in this.favoriteList) {
      const queryURL = this.serverURL + '/price?symbol=' + this.favoriteList[i].symbol;
      this.updateStockData(i, queryURL); //detectChanges() in updateStockData()
    }
  }

  /**
   * request favorite stock data and update favoriteList
   */
  updateStockData(index, queryURL){
    // console.log('before refresh ' + this.favoriteList[index].symbol + ", price: " + this.favoriteList[index].price);
    this.http.get(queryURL).subscribe(data => {
        try {
          let meta_data = data['Meta Data']; 
          let json_series_data = data['Time Series (Daily)']; 
          let symbol = this.favoriteList[index].symbol;

          let prevPrice = this.favoriteList[index].price;
          let newOpen, newVolume;
          for (let key in json_series_data) { //just get one data, then break
              newOpen = json_series_data[key]['1. open'];
              newVolume = json_series_data[key]['5. volume'];
              break;
          }

          //5 seconds is too fast for api querying, may crush, leads to undefined
          if(newOpen === undefined){
            this.favoriteList[index].change = 0;
            this.favoriteList[index].changePercent = 0.00;
          }else{
            let temChange = (parseFloat(newOpen) - prevPrice);
            this.favoriteList[index].change = parseFloat(temChange.toFixed(2));
            if(prevPrice === 0)
              this.favoriteList[index].changePercent = 0; //avoid divided by 0
            else
              this.favoriteList[index].changePercent = parseFloat((temChange / prevPrice * 100).toFixed(2));
            this.favoriteList[index].price = parseFloat(newOpen);
          }

          if(newVolume === undefined)
            this.favoriteList[index].volume = 'waiting';
          else
            this.favoriteList[index].volume = newVolume.replace(/(?=(?:\d{3})+\b)/g, ',');

          //subscribe is async funtion, have to save changes and update list here
          //if in for loop, then not updated, since for loop runs over before data is fetched
          localStorage.setItem('favoriteList', JSON.stringify(this.favoriteList)); //save changes
          //to update UI, if no detectChanges, UI won't change even if date updated
          this.ref.detectChanges(); 
        } catch (error) {
          console.log(error);
        }
      },
      err => {
        console.log(err);
      });
  }


  /**
   * sorting key changes, eg. sort by symbol, price, volume...
   * @param value 
   */
  onSortingKeyChange(value){
    this.orderKey = value;
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
  }

  /**
   * add (or remove) stock to favorite list
   */
  addToFavorite(){
    let favoriteCheckRes = this.isStockInFavoriteList(this.symbolName);
    if(favoriteCheckRes.found){ //if in favorite List, remove it change ang img to empty-star
      // document.getElementById("btn_fav").className = "glyphicon glyphicon-star-empty";
      this.removeCurrentStockFromFavoriteList(favoriteCheckRes.index);
    }else{  //else add to favorite List, and change img to star
      // document.getElementById("btn_fav").className = "glyphicon glyphicon-star";
      this.addCurrentStockToFavoriteList();
    }
    this.currentStockInFavoriteList = !this.currentStockInFavoriteList;
    localStorage.setItem('favoriteList', JSON.stringify(this.favoriteList));
  }

  /**
   * remove stock at specified index, 'star' button calls addToFavorite(), may call this function
   * @param index 
   */
  removeCurrentStockFromFavoriteList(index){
    this.favoriteList.splice(index, 1);
    console.log(this.favoriteList);
  }

  /**
   * check whether current searching stock is in user's favorite list
   * if so, return true and its index; otherwise, return false and -1
   */
  isStockInFavoriteList(symbol){
    this.favoriteList = JSON.parse(localStorage.getItem('favoriteList'));
    let found = false, index = -1;
    if(this.favoriteList === null || this.favoriteList === undefined || this.favoriteList.length === 0){
      return {found, index}; //false, -1
    }
    for(let i = 0; i < this.favoriteList.length; i++) {
        if (this.favoriteList[i].symbol == symbol) {
            found = true; index = i;
            break;
        }
    }
    return {found, index};
  }

  /**
   * client trash button calls this function to delete stock from favorite list
   * @param symbol 
   */
  removeSpecificStockFromFavoriteList(symbol){
    console.log("delete: " + symbol);
    //When delete button clicked, remove stock from the table and favorites list local storage
    let res = this.isStockInFavoriteList(symbol);
    this.favoriteList.splice(res.index, 1);
    localStorage.setItem('favoriteList', JSON.stringify(this.favoriteList)); //save changes

    //When delete button clicked, the removed stock should not be displayed with yellow star in other pages
    if(symbol == this.symbolName)
      this.currentStockInFavoriteList = false;
  }


  /**
   * add current stock to favorite list
   */
  addCurrentStockToFavoriteList(){
    let addTime = Date.now();
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
    let imgURL = encodeURI('async=false&type=jpeg&width=600&options=' + chartOptions); //body
    console.log(imgURL);
    console.log(imgURL.length);
    
    // let exportImgURL = this.serverURL + "/postImg";
    let exportImgURL = "http://localhost:3000/postImg";

    //post to server
    this.http.post(exportImgURL, imgURL, {
      responseType: 'text',
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8')
                .append('accept', '*/*')
    }).subscribe(res => {
      //res looks like "http://export.highcharts.com/charts/chart.472758adeb194a56a54b4479c63810eb.png"
      console.log(res);
      let params: UIParams = {
        method: 'feed',
        link: res  //Full URL
      }
      this.fb.ui(params)
        .then((res: UIResponse) => {
          alert('Shared Successfully');
          console.log(res);
        }).catch((e: any) => {
          alert('Not Posted');
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
    // console.log("draw line: " + symbol);

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
   * check whether input is valid (not empty and not only contains space)
   * @param value 
   */
  inputValidationCheck(value): boolean{
    length = value.replace(/\s/g, '').length;
    let isValid = (length !== 0);
    if(isValid)
      this.isInvalidInput = false;
    else
      this.isInvalidInput = true; //empty
    this.ref.detectChanges();
    console.log("is input valid: " + isValid);
    return isValid;
  }

  /**
   * query:
   * (1) price & volume data
   * (2) all indicators 
   * @param value 
   */
  async onSubmit(value) {
    //reset to false (no err in fetching data)
    this.errInloadingMap= {'Price': false, 'SMA': false, 'EMA': false, 'STOCH': false, 'RSI': false,'ADX': false,
    'CCI': false, 'BBANDS': false, 'MACD': false, 'News': false};
    //set to false, (still waiting for data coming)
    this.loadingMap = {'Price': false, 'SMA': false, 'EMA': false, 'STOCH': false, 'RSI': false,'ADX': false,
    'CCI': false, 'BBANDS': false, 'MACD': false, 'Table': false, 'HighStock': false, 'News': false};

    this.existStockInfo = true;
    this.isInvalidInput = false;
    this.validQuote = false;

    if(this.inputValidationCheck(value) == false)
      return;

    if(this.isInFavoriteListPage)
      this.switchDivAnimate();
    this.isInFavoriteListPage = false;

    // let data = await this.service.queryPrice(value);
    let baseURL = this.serverURL + '/price?symbol=';
    console.log("onSubmit: " + baseURL + value);

    this.http.get(baseURL + value).subscribe(data => {
      try {  
        let meta_data = data['Meta Data']; 
        let json_series_data = data['Time Series (Daily)']; 
        let parseRes = this.chartService.parsePriceData(json_series_data);
    
        //update table
        this.symbolName = value;
        //YYYY-MM-DD when closed or YYYY-MM-DD HH:mm:ss when open
        let lastRefreshedTime = meta_data['3. Last Refreshed'].toString();
        if(lastRefreshedTime.length <= 12) //"2017-11-07"
          lastRefreshedTime += " 16:00:00";
        this.timestamp = lastRefreshedTime + " " + this.chartService.getTimeZoneName(lastRefreshedTime, "US/Eastern");
          
        //cur day (key) is Object.keys(json_series_data)[0]
        let curObj = json_series_data[Object.keys(json_series_data)[0]]; //current day
        let prevObj = json_series_data[Object.keys(json_series_data)[1]]; //previous day
        this.curOpen = parseFloat(curObj['1. open']).toFixed(2);
        this.curClose = parseFloat(curObj['4. close']).toFixed(2);
        this.curRange = parseFloat(curObj['3. low']).toFixed(2) + ' - ' + parseFloat(curObj['2. high']).toFixed(2);
        this.curVolume = curObj['5. volume'].toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");;
    
        this.lastPrice = parseFloat(prevObj['4. close']).toFixed(2);    
        this.changeNum = (this.curClose - this.lastPrice).toFixed(2);
        this.changePercent = ((this.changeNum / this.lastPrice) * 100).toFixed(2) + "%";
  
        this.loadingMap['Table'] = true;

        if(this.isStockInFavoriteList(this.symbolName).found == true)
          this.currentStockInFavoriteList = true;
        else
          this.currentStockInFavoriteList = false;

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
              type: 'area', name: 'Pirce', yAxis:0,
              data: parseRes.price, //data
              tooltip:{ pointFormat: value + ': {point.y:,..2f}' },
              marker:{ enabled:false }
            },
            {
              type: 'column', name: 'Volume', yAxis:1, color: 'red',
              data: parseRes.volume
            }
          ]
        };
        this.createStockChart(json_series_data);
        this.loadingMap['Price'] = true; //data loaded
      } catch (error) {
        this.errInloadingMap['Pirce'] = true;
        console.log(error);
      }
    },
    err => {
      this.errInloadingMap['Price'] = true;
      console.log(err);
    });

    this.createNewArray(value, "US/Eastern"); //request news
    //The order is this.http.get() and drawLineCharts() run simultaneously
    //after onSubmit()'s code finished, then createStockChart() and createNewArray()
    this.drawLineCharts(value);
  }

  createNewArray(symbol, timeZone){
    let baseURL = this.serverURL + '/news?symbol=';
    console.log("createNewArray: " + baseURL + symbol);
    this.http.get(baseURL + symbol).subscribe(data => {
      //parse, get 5 news and convert news link if needed
      let limit = 5;
      this.newsArray = this.chartService.parseNew(data, timeZone, limit); //jsonObj
      // console.log(this.newsArray);
      this.loadingMap['News'] = true;
    },
    err => {
      this.errInloadingMap['News'] = true;
      console.log(err);
      console.log("error in query news: " + this.errInloadingMap['News']);
    });
  }

  /**
   * draw historical stock chart
   * @param data 
   */
  createStockChart(data){
    //[[1383202800000, 35.405], [1383289200000, 35.525], ... [1508396400000, 77.91]]
    let parseRes = this.chartService.parseStockData(data); //1000 elements
    this.stockChartOptions = {
      chart: { height: 400, width: null },
      title: { text: this.symbolName + ' Stock Value' },
      subtitle: {
          useHTML:true,
          text: "<a target='_blank' style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
      },
      rangeSelector: { selected: 1 },
      series: [{
          name: this.symbolName,
          data: parseRes,
          type: 'area',
          threshold: null,
          tooltip: { valueDecimals: 2 }
      }],
      responsive: {
          rules: [{
              condition: { maxWidth: 500 },
              chartOptions: {
                  chart: { height: 300 },
                  subtitle: { text: null },
                  navigator: { enabled: false }
              }
          }]
        }
      }
    this.loadingMap['HighStock'] = true;
  }

  /**
   * drwa a single line chart
   * @param indexMap 
   * @param symbol 
   * @param indicator 
   */
  drawSingleLineChart(indexMap, symbol, indicator){
    let baseURL = this.serverURL + "/indicator?symbol=" + symbol;
    console.log("drawSingleLineChart: " + baseURL + '&indicator=' + indicator);
    this.http.get(baseURL + '&indicator=' + indicator).subscribe(data => {
      // console.log(data);
      let indicator_data = data['Technical Analysis: ' + indicator]; //full size data
      
      if(indicator_data === undefined){
        this.errInloadingMap[indicator] = true;
        return;
      }

      let parseRes = this.chartService.parseSingleTarget(indicator_data, indicator);
      let singleLineCharOption = {
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
      this.loadingMap[indicator] = true;
    },
    err => {
      this.errInloadingMap[indicator] = true;
      console.log(err);
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
    let baseURL = this.serverURL + "/indicator?symbol=" + symbol;
    let isTwoLine = false;
    if(target3.length == 0)
      isTwoLine = true;

    this.http.get(baseURL + '&indicator=' + indicator).subscribe(data => {
      // console.log(data);
      let indicator_data = data['Technical Analysis: ' + indicator]; //full size data
      if(indicator_data === undefined){
        this.errInloadingMap[indicator] = true;
        return;
      }
      let parseRes;
      if(isTwoLine == true)
        parseRes = this.chartService.parseTwoTarget(indicator_data, target1, target2); //SlowD, SlowK
      else 
        parseRes = this.chartService.parseThreearget(indicator_data, target1, target2, target3); 

        let mutipleLineChartOption = {
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
        tooltip: { crosshairs: true, shared: true },
        series: [{
            name: '', data: [], lineWidth: 1,
            marker: { enabled: true, symbol:'square', radius: 2 }
          },
          {
            name: '', data: [], lineWidth: 1,
            marker: { enabled: true, symbol:'square', radius: 2 }
          }
        ]
      };

      mutipleLineChartOption['title']['text'] = indicator;
      mutipleLineChartOption['xAxis']['categories'] = parseRes.date;
      mutipleLineChartOption['series'][0]['name'] = symbol + ' ' + target1;
      mutipleLineChartOption['series'][0]['data'] = parseRes.indicator_1;
      mutipleLineChartOption['series'][1]['name'] = symbol + ' ' + target2;
      mutipleLineChartOption['series'][1]['data'] = parseRes.indicator_2;

      if(isTwoLine == false){ //three lines
        //add a new series
        let newSeries = {'name':'', 'data':[], 'lineWidth': 1, 'marker': { enabled: true, symbol:'square', radius: 2 }};
        newSeries['name'] = symbol + ' ' + target3;
        newSeries['data'] = parseRes.indicator_3;
        mutipleLineChartOption['series'].push(newSeries);
      }

      switch(indexMap[indicator]){
        case 3:{ this.STOCHChartOptions = mutipleLineChartOption; break; } 
        case 7:{ this.BBANDSChartOptions = mutipleLineChartOption; break; }
        case 8:{ this.MACDChartOptions = mutipleLineChartOption; break; }
      }
    
      this.loadingMap[indicator] = true;
    },
    err => {
      this.errInloadingMap[indicator] = true;
      console.log(err);
    });
  }
}