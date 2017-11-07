export const configs = [
    {
        //save space for price chart
    },
    {   
        //SMA chart
        chart: {
            zoomType: 'x'
        },
        title: {
            text: 'SMA'
        },
        subtitle: {
            useHTML:true,
            text: "<a style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
        },
        xAxis: {
            categories: [],
            tickPositioner: function() {
                let res = [];
                for(let i = 0; i < this.categories.length; i++) {
                    if(i % 5 == 0) 
                        res.push(this.categories.length - 1 - i);
                }
                return res;
            }
        },
        yAxis: {
            title: {
                text: 'SMA'
            }
        },
        plotOptions: {
            states: {
                hover: {
                    lineWidth: 1
                }
            }
        },
        series: [{
            name: '',
            color: '#E62516',
            lineWidth: 1,
            marker: {
                enabled: true,
                symbol:'square',
                radius: 2,
                fillColor: '#E62516'
            },
            data: []
        }]
    },
    {   
        //EMA chart
        chart: {
            zoomType: 'x'
        },
        title: {
            text: 'EMA'
        },
        subtitle: {
            useHTML:true,
            text: "<a style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
        },
        xAxis: {
            categories: [],
            tickPositioner: function() {
                let res = [];
                for(let i = 0; i < this.categories.length; i++) {
                    if(i % 5 == 0) 
                        res.push(this.categories.length - 1 - i);
                }
                return res;
            }
        },
        yAxis: {
            title: {
                text: 'EMA'
            }
        },
        plotOptions: {
            states: {
                hover: {
                    lineWidth: 1
                }
            }
        },
        series: [{
            name: '',
            color: '#E62516',
            lineWidth: 1,
            marker: {
                enabled: true,
                symbol:'square',
                radius: 2,
                fillColor: '#E62516'
            },
            data: []
        }]
    },
    {
        //STOCH chart
        chart: {
            zoomType: 'x'
        },
        title: {
            text: 'STOCH'
        },
        subtitle: {
            useHTML:true,
            text: "<a style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
        },
        xAxis: {
            categories: [],
            tickPositioner: function() {
                let res = [];
                for(let i = 0; i < this.categories.length; i++) {
                    if(i % 5 == 0) 
                        res.push(this.categories.length - 1 - i);
                }
                return res;
            }
        },
        yAxis: {
            title: {
                text: 'STOCH'
            }
        },
        tooltip: {
            crosshairs: true,
            shared: true
        },
        
        series: [{
            name: '',
            color: '#96C7EF',
            data: [],
            lineWidth: 1,
            marker: {
                enabled: true,
                symbol:'square',
                radius: 2
            }
        },
        {
            name: '',
            color: '#F29593',
            data: [],
            lineWidth: 1,
            marker: {
                enabled: true,
                symbol:'square',
                radius: 2
            }
        }]
    },
    {   
        //RSI chart
        chart: {
            zoomType: 'x'
        },
        title: {
            text: 'RSI'
        },
        subtitle: {
            useHTML:true,
            text: "<a style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
        },
        xAxis: {
            categories: [],
            tickPositioner: function() {
                let res = [];
                for(let i = 0; i < this.categories.length; i++) {
                    if(i % 5 == 0) 
                        res.push(this.categories.length - 1 - i);
                }
                return res;
            }
        },
        yAxis: {
            title: {
                text: 'RSI'
            }
        },
        plotOptions: {
            states: {
                hover: {
                    lineWidth: 1
                }
            }
        },
        series: [{
            name: '',
            color: '#E62516',
            lineWidth: 1,
            marker: {
                enabled: true,
                symbol:'square',
                radius: 2,
                fillColor: '#E62516'
            },
            data: []
        }]
    },
    {   
        //ADX chart
        chart: {
            zoomType: 'x'
        },
        title: {
            text: 'ADX'
        },
        subtitle: {
            useHTML:true,
            text: "<a style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
        },
        xAxis: {
            categories: [],
            tickPositioner: function() {
                let res = [];
                for(let i = 0; i < this.categories.length; i++) {
                    if(i % 5 == 0) 
                        res.push(this.categories.length - 1 - i);
                }
                return res;
            }
        },
        yAxis: {
            title: {
                text: 'ADX'
            }
        },
        plotOptions: {
            states: {
                hover: {
                    lineWidth: 1
                }
            }
        },
        series: [{
            name: '',
            color: '#E62516',
            lineWidth: 1,
            marker: {
                enabled: true,
                symbol:'square',
                radius: 2,
                fillColor: '#E62516'
            },
            data: []
        }]
    },
    {   
        //CCI chart
        chart: {
            zoomType: 'x'
        },
        title: {
            text: 'CCI'
        },
        subtitle: {
            useHTML:true,
            text: "<a style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
        },
        xAxis: {
            categories: [],
            tickPositioner: function() {
                let res = [];
                for(let i = 0; i < this.categories.length; i++) {
                    if(i % 5 == 0) 
                        res.push(this.categories.length - 1 - i);
                }
                return res;
            }
        },
        yAxis: {
            title: {
                text: 'CCI'
            }
        },
        plotOptions: {
            states: {
                hover: {
                    lineWidth: 1
                }
            }
        },
        series: [{
            name: '',
            color: '#E62516',
            lineWidth: 1,
            marker: {
                enabled: true,
                symbol:'square',
                radius: 2,
                fillColor: '#E62516'
            },
            data: []
        }]
    },
    {
        //BBANDS chart
        chart: {
            zoomType: 'x'
        },
        title: {
            text: 'BBANDS'
        },
        subtitle: {
            useHTML:true,
            text: "<a style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
        },
        xAxis: {
            categories: [],
            tickPositioner: function() {
                let res = [];
                for(let i = 0; i < this.categories.length; i++) {
                    if(i % 5 == 0) 
                        res.push(this.categories.length - 1 - i);
                }
                return res;
            }
        },
        yAxis: {
            title: {
                text: 'BBANDS'
            },
            marker:{
                enabled: true,
                symbol:'square',
                radius: 1,
            },
        },
        tooltip: {
            crosshairs: true,
            shared: true
        },
        series: [{
            name: '',
            data: [],
            lineWidth: 1,
            marker: {
                enabled: true,
                symbol:'square',
                radius: 2
            }
        },
        {
            name: '',
            // color: 'red',
            data: [],
            lineWidth: 1,
            marker: {
                enabled: true,
                symbol:'square',
                radius: 2
            }
        },
        {
            name: '',
            data: [],
            lineWidth: 1,
            marker: {
                enabled: true,
                symbol:'square',
                radius: 2
            }
        }]
    },
    {
        //MACD chart
        chart: {
            zoomType: 'x'
        },
        title: {
            text: 'MACD'
        },
        subtitle: {
            useHTML:true,
            text: "<a style='text-decoration: none' href='https://www.alphavantage.co/'>Source: Alpha Vantage</a>"
        },
        xAxis: {
            categories: [],
            tickPositioner: function() {
                let res = [];
                for(let i = 0; i < this.categories.length; i++) {
                    if(i % 5 == 0) 
                        res.push(this.categories.length - 1 - i);
                }
                return res;
            }
        },
        yAxis: {
            title: {
                text: 'MACD'
            },
            marker:{
                enabled: true,
                symbol:'square',
                radius: 1,
            },
        },
        tooltip: {
            crosshairs: true,
            shared: true
        },
        series: [{
            name: '',
            data: [],
            lineWidth: 1,
            marker: {
                enabled: true,
                symbol:'square',
                radius: 2
            }
        },
        {
            name: '',
            // color: 'red',
            data: [],
            lineWidth: 1,
            marker: {
                enabled: true,
                symbol:'square',
                radius: 2
            }
        },
        {
            name: '',
            data: [],
            lineWidth: 1,
            marker: {
                enabled: true,
                symbol:'square',
                radius: 2
            }
        }]
    }
]