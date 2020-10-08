/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.blakfx.crypto;

public final class invokeStatus_t {
  public final static invokeStatus_t INVOKE_STATUS_NOT_INITIALIZED = new invokeStatus_t("INVOKE_STATUS_NOT_INITIALIZED", -255);
  public final static invokeStatus_t INVOKE_IN_INVALID_STATE = new invokeStatus_t("INVOKE_IN_INVALID_STATE", -254);
  public final static invokeStatus_t INVOKE_INVALID_INSIDE_CALLBACK = new invokeStatus_t("INVOKE_INVALID_INSIDE_CALLBACK", -253);
  public final static invokeStatus_t INVOKE_STATUS_BAD_PROMISE_ID = new invokeStatus_t("INVOKE_STATUS_BAD_PROMISE_ID", -252);
  public final static invokeStatus_t INVOKE_STATUS_TIMEOUT = new invokeStatus_t("INVOKE_STATUS_TIMEOUT", -2);
  public final static invokeStatus_t INVOKE_STATUS_FALSE = new invokeStatus_t("INVOKE_STATUS_FALSE", -1);
  public final static invokeStatus_t INVOKE_STATUS_TRUE = new invokeStatus_t("INVOKE_STATUS_TRUE", 0);

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static invokeStatus_t swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + invokeStatus_t.class + " with value " + swigValue);
  }

  private invokeStatus_t(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private invokeStatus_t(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private invokeStatus_t(String swigName, invokeStatus_t swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static invokeStatus_t[] swigValues = { INVOKE_STATUS_NOT_INITIALIZED, INVOKE_IN_INVALID_STATE, INVOKE_INVALID_INSIDE_CALLBACK, INVOKE_STATUS_BAD_PROMISE_ID, INVOKE_STATUS_TIMEOUT, INVOKE_STATUS_FALSE, INVOKE_STATUS_TRUE };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}
