import { AfterViewInit, Component, ElementRef, EventEmitter, forwardRef, Input, NgZone, Output, ViewChild } from "@angular/core";
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from "@angular/forms";

declare let $: any;

@Component({
    selector: "toggle",
    template: `<input #togglecheckbox type="checkbox" data-toggle="toggle" attr.data-on="{{onText}}" attr.data-off="{{offText}}" attr.data-onstyle="{{onStyle}}" attr.data-offstyle="{{offStyle}}" attr.data-size="{{size}}" />`,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => InputToggleComponent),
            multi: true
        }
    ]
})

export class InputToggleComponent implements AfterViewInit, ControlValueAccessor {

    private _propagateChange = (_: any) => { };
    private _propogateTouched = (_: any) => { };

    @Input() onText: string = "On";
    @Input() offText: string = "Off";
    @Input() onStyle: string = "primary";
    @Input() offStyle: string = "default";
    @Input() size: string = "normal";

    @Input('checked') _checked: boolean = false;
    @ViewChild("togglecheckbox") private _checkbox: ElementRef;

    public get checked(): boolean {
        return this._checked;
    }
    public set checked(value: boolean) {
        this.zone.run(() => {
            this._checked = value;
            this._propagateChange(value);
        });
    }

    constructor(private zone: NgZone) {
    }

    ngAfterViewInit() {
        this._checkbox.nativeElement.checked = this.checked;
        $(this._checkbox.nativeElement).bootstrapToggle();
        $(this._checkbox.nativeElement).change((event: any) => this.toggleValueChanged(event));
        this.checked = this.checked;
    }

    toggleValueChanged(event: any) {
        this.checked = !!this._checkbox.nativeElement.checked;
    }

    writeValue(obj: any) {
        if (obj)
            this.checked = !!obj;
    }

    registerOnChange(fn: any) {
        this._propagateChange = fn;
    }

    registerOnTouched(fn: any) {
        this._propogateTouched = fn;
    }
}