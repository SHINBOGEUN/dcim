package net.vivans.dcim.module.devicemodel.domain.model;

public enum ModbusRegisterType {
    COIL("readCoils"),
    DISCRETE("readDiscreteInputs"),
    HOLDING("readHoldingRegisters"),
    INPUT("readInputRegisters");

    private final String jsReadFunction;
    ModbusRegisterType(String jsReadFunction) {
        this.jsReadFunction = jsReadFunction;
    }
    public String getJsReadFunction() {
        return jsReadFunction;
    }
}
