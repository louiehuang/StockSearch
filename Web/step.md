# Compile

`ng build` is for develop mode, vendor bundle is huge...

https://stackoverflow.com/questions/41432673/angular2-cli-huge-vendor-bundle-how-to-improve-size-for-prod

can use `ng build --prod --aot`


http://blog.mgechev.com/2016/06/26/tree-shaking-angular2-production-build-rollup-javascript/

What's more, use gzip to compress minified files. 

in terminal, `gzip filename`

In nginx.conf, turn on gzip

```
gzip on;
gzip_disable "MSIE [1-6]\.(?!.*SV1)";
gzip_proxied any;
gzip_buffers 16 8k;
gzip_types    text/plain application/javascript application/x-javascript text/javascript text/xml text/css;
gzip_vary on;
```

https://varvy.com/pagespeed/enable-compression.html



## 1.frontend

Folder: /Users/hlyin/Documents/Angular/Stock, 
create a folder in `Stock` folder

`ng new frontend`

In VS code, open frontend folder


**`ng serve -o`**



## 2.backend

**`nodemon ./server.js`**

> 1. create backend folder
> 2. open intergrated temrinal
> 3. input `npm init` to create package.json file
> 4. npm install express --save
> 5. create server.js file
> 6.`node ./server.js`

install nodemon to update node file in real time(no need to stop and start for file changes)
`sudo npm install -g nodemon`

Then we can run node file using **`nodemon ./server.js`**

//do not get data from server inside the constructor,
//it will delay our app and component initialization as the date comes in!


```typescript
//front end, web.service
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import 'rxjs/add/operator/toPromise';


@Injectable()
export class WebService{
    constructor(private http : Http){}

    getMessages(){
        return this.http.get('http://localhost:1234/messages').toPromise();
    }
}
```

```typescript
//front end, messages.component.ts
import { Component } from '@angular/core'
import { WebService } from './web.service'

@Component({
    selector: 'messages',
    template: `
        <div *ngFor="let message of messages"> 
            <mat-card style="margin:8px">
                <mat-card-title>{{message.text}}</mat-card-title>
                <mat-card-content>{{message.owner}}</mat-card-content>
            </mat-card>
        </div>
    `
})
export class MessagesComponent {
    constructor(private webService : WebService){
        //do not get data from server inside the constructor,
        //it will delay our app and component initialization as the date comes in!
        //instead, we can use a function called "ngOnInit", which gets called once the component
        //is done intializing and after the constructor gets called.
    }

    async ngOnInit(){
        var response = await this.webService.getMessages();
        console.log(response.json());
        this.messages = response.json();
    }

    //will be replaced to messages fetched from server
    messages = [];
}
```


```javascript
//backend, server.js
var express = require('express'); 
var app = express();
var messages = [{text: 'I want it that way', owner: 'BSB'},{text: 'New Song', owner: 'admin'}];

app.use((req, res, next) => {
    res.header ("Access-Control-Allow-Origin", "*");
    res.header ("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    next(); // pass control to the next handler
})

app.get('/', (req, res) => {
    res.send('Hello');
})

app.get('/messages', (req, res) => {
    res.jsonp(messages);
})

app.listen(1234);

```

Also, remember to modift `tsconfig.json`, change `"target": "es5"` to `"target": "es6"` 


`npm install request request-promise`