/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package io.zenwave360.modulith.events.scs.dtos.avro;

@org.apache.avro.specific.AvroGenerated
public enum PaymentMethodType implements org.apache.avro.generic.GenericEnumSymbol<PaymentMethodType> {

    VISA, MASTERCARD;

    public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse(
            "{\"type\":\"enum\",\"name\":\"PaymentMethodType\",\"namespace\":\"io.zenwave360.modulith.events.scs.dtos.avro\",\"symbols\":[\"VISA\",\"MASTERCARD\"]}");

    public static org.apache.avro.Schema getClassSchema() {
        return SCHEMA$;
    }

    @Override
    public org.apache.avro.Schema getSchema() {
        return SCHEMA$;
    }

}
