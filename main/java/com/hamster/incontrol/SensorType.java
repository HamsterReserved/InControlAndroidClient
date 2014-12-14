package com.hamster.incontrol;

/**
 * 传感器类型，从0起始，后续有待添加。
 * 请保持与云端PHP、单片机程序中的int值一致！
 */
public enum SensorType {
    SENSOR_LIGHT,
    SENSOR_ELECTRICITY,
    SENSOR_SWITCH
}
