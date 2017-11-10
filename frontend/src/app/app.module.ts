import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';

import { FormsModule, ReactiveFormsModule }   from '@angular/forms';
import {MatAutocompleteModule} from '@angular/material';
import {AppService} from './app.service';
import {ChartsService} from './charts.service';
import { HttpModule } from '@angular/http';

import { ChartModule } from 'angular2-highcharts';
import { HighchartsStatic } from 'angular2-highcharts/dist/HighchartsService';

import { HttpClientModule } from '@angular/common/http';
import { MomentModule } from 'angular2-moment/moment.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { OrderModule } from 'ngx-order-pipe';
import { FacebookModule } from 'ngx-facebook';
import { BootstrapSwitchComponent } from 'angular2-bootstrap-switch';

declare let require: any;
export function highchartsFactory() {
  const hc = require('highcharts/highstock');
  const dd = require('highcharts/modules/exporting');
  dd(hc);
  return hc;
}

@NgModule({
  declarations: [
    AppComponent, BootstrapSwitchComponent
  ],
  imports: [
    BrowserModule, NgbModule,
    FormsModule, ReactiveFormsModule, MatAutocompleteModule,
    HttpModule,
    ChartModule,
    FacebookModule.forRoot(),
    HttpClientModule,
    MomentModule,
    BrowserAnimationsModule,
    OrderModule
  ],
  providers: [{
    provide: HighchartsStatic,
    useFactory: highchartsFactory
    },
    AppService, 
    ChartsService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

