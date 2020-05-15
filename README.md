[![Build Status](https://dev.azure.com/OmniscientJVM/OMJ/_apis/build/status/OmniscientJVM.OMJ?branchName=master)](https://dev.azure.com/OmniscientJVM/OMJ/_build/latest?definitionId=1&branchName=master)
[![codecov](https://codecov.io/gh/OmniscientJVM/OMJ/branch/master/graph/badge.svg)](https://codecov.io/gh/OmniscientJVM/OMJ)

# OMJ: An Omniscient Debugger for the JVM

## Method Tracing

- Instrument the start of each method to:
 - Load the method identifier string
 - Allocate the `MethodTrace` implementation that matches the method descriptor
 - Call `methodCall_start`
 - Call `methodCall_argument_x` with each argument
 - Call `methodCall_end`
- `methodCall_start` allocates a generated POJO that matches the argument types of the method
- `methodCall_argument_x` sets each field of that POJO
- `methodCall_end` pushes the POJO instance onto a queue for another thread to process
