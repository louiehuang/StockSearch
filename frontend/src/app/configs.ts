export const configs = [
    {   
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
            name: 'AAPL SMA',
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
            name: 'AAPL EMA',
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
    }
]