[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/open-amdocs/textcrate.svg?branch=master)](https://travis-ci.org/open-amdocs/textcrate)

# TextCrate Message Repositories

The main purpose of this library is to enable external repositories of parametrized messages for easy modification,
localization, proof-reading, and automatic checking at build-time, as well as to provide convenient access to the
messages from the code of a Java application.

## Example

### Message Repository

```java
@CodeSpec(offset=20, pattern="BOR-{}")
@MessageProperty(name="type", value="Error")
public interface BookBorrowingErrors {
    
    @MessageSpec(id=1, pattern="'{}' is currently not available")
    Message bookUnavailable(String title);
    
    @MessageSpec(id=2, pattern="Attempt to borrow more than {} books")
    Message attemptToBorrowMoreThanAllowed(int maxAllowed);
}
```

### Printing to Stdout

```java
    BookBorrowingErrors borrowingErrors = Messages.from(BookBorrowingErrors.class);

    Message unavailable = borrowingErrors.bookUnavailable("The Mythical Man-Month");
    
    // Prints "[BOR-21] 'The Mythical Man-Month' is currently not available" to stdout
    System.out.printf("[%s] %s\n", unavailable.getCode(), unavailable.getMessage());   
```

### SLF4J Logging

```java
    BookBorrowingErrors borrowingErrors = Messages.from(BookBorrowingErrors.class);
   
    Message reachedMaxBooks = borrowingErrors.attemptToBorrowMoreThanAllowed(12);
    
    // Logs "Attempt to borrow more than 12 books" in SLF4J format 
    if ("Error".equals(reachedMaxBooks.getProperty("type"))) {
        LOGGER.error(reachedMaxBooks.getPattern(), reachedMaxBooks.getArguments());
    }
```

Note, that in a real application `"type"` and `"Error"` will probably be constants. 
The instance of `BookBorrowingErrors` is also likely to be kept in a constant.

## Motivation

We were looking for an application message repository API that would be easy to use, and would allow for streamlined 
development. Adding or using a message should require minimum disruption from a developer, and be IDE friendly. 

Actually, adding a new message type to a repository, and instantiating a message object should be as easy as adding and 
calling a method! This is easily done "on the fly" in any modern IDE. This approach would also automatically enforce the
uniqueness and number of parameters of a message, and allow for typed parameters as an additional advantage if needed. 
Auto code completion when selecting/using an existing message is yet another advantage.

## Modules

The library includes modules as follows:

1. The API module is the only one that application code should see. It contains application-facing interfaces and
   factories, as well as some basic classes that can be used by a concrete implementation of message loading and
   formatting.
      
   This module also exposes a Service Provider Interface (SPI) to allow custom mechanisms for message loading and 
   creation.
   
   If no service providers are configured, the default implementation based on Java dynamic proxy is used.  

2. _Work in Progress_: A compiler that generates optimized code for efficient loading of message repositories. It may
   also validate the correct return type, message parameters and formatting rules in build time, as well as check that 
   external message sources (i.e. message bundles) contain all the messages defined by an interface. 

   Possible validations:
   - A default message must be specified.
   - The format must match the formatter.
   - Each message must exist in the available external repositories.
   - The type and number of parameters must be correct.
   - Enforce message code uniqueness per repository.

3. _Work in Progress_: Tools (e.g. Maven plugin) for generating user documentation showing message codes and their 
  descriptions.

## TODO

- Support inheritance, when an interface extends another interface or even multiple interfaces.
 
- Support interfaces declared inside classes.

- Implement loading non-default formatting patterns using Java Resource Bundles.

- Implement message localization in the API module. Currently, only the default formatting pattern is read.

## Code Style

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
