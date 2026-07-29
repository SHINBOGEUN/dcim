package net.vivans.dcim.module.devicemodel.domain.model;

public enum ModbusByteOrder {
    ABCD,   // Big Endian
    CDAB,   // Word Swapped
    BADC,   // Byte Swapped
    DCBA;   // Little Endian
}