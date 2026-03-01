package com.example.practice.common.error;

public class EnvDataNotFoundException extends BusinessException {
  public EnvDataNotFoundException(Long envDataId) {
    super(ErrorCode.ENV_DATA_NOT_FOUND, "envDataId: " + envDataId);
  }
}
