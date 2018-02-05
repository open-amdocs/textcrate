[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/open-amdocs/zusammen.svg?branch=master)](https://travis-ci.org/open-amdocs/message-formatting)

# Message Formatting

The main purpose of this library is to enable external repositories of parametrized messages for easy modification,
localization, proof-reading, and automatic checking at build-time, as well as to provide convenient access to the
messages from application code.

## Modules

The library includes modules as follows:

1. The API module is the only one that application code should see. It contains application-facing interfaces and
   factories, as well as some basic classes that can be used by a concrete implementation of message loading and
   formatting.
      
   This module also exposes a Service Provider Interface (SPI) to allow custom mechanisms for message loading and 
   creation.
   
   If no service providers are configured, the default implementation based on Java dynamic proxy is used.  

2. _Work in Progress_: A compiler that generates code for efficient loading of message repositories. This compiler may
   also validate the correct return type, message parameters and formatting rules in build time, as well as check that 
   external message sources (i.e. message bundles) contain all the messages defined by an interface. 

3. _Work in Progress_: Tools (e.g. Maven plugin) for generating user documentation showing message codes and their 
  descriptions.

## TODO

- Implement loading non-default formatting patterns using Java Resource Bundles.

- Implement message localization in the API module. Currently, only the default formatting pattern is read.

## Notes

- The style and coding convention used for this project are based on 
  [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html), with the exception of line length 
  (120 instead of 100), and indentation size (4 spaces instead of 2). 

## License

    Copyright © 2016-2018 European Support Limited
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.  
