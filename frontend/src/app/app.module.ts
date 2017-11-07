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

declare var require: any;

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule, NgbModule,
    FormsModule, ReactiveFormsModule, MatAutocompleteModule,
    HttpModule,
    ChartModule.forRoot(require('highcharts'), require('highcharts/modules/exporting')),
    HttpClientModule
  ],
  providers: [AppService, ChartsService],
  bootstrap: [AppComponent]
})
export class AppModule { }
