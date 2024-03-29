implement a stamp card loyalty system of the type seen in many cafes and coffee shops (e.g. buy 4 coffees get the 5th free).

### Setup
An interface to implement is provided `ImplementMe`.  The methods are documented to explain what is expected.  Model classes are provided to model some of the basics - please create any extra models that you want but also please don't modify the models given.

Some basic test cases are also provided in `LoyaltySpec`

The `ImplementMe` interface has a `schemes` property - the calling code will set this property to represent the active schemes that should be run on any `apply` call.  The schemes will not change between runs and should be considered immutable


In the real world - this would exist as a service, communicating over REST with other services and persisting to a database.  However in order to keep the test focused on the interesting parts the following assumptions are made:
* The `ImplementMe` interface represents the API spec to implement, the return types represent the response bodies that would be used and *do not* represent an opinion on the data structure to use.
* Persistence is not required - the code should store it's state in memory and is not expected to survive restarts.
* Multiple schemes can be running for one merchant concurrently.
* When an account is due a payment the cheapest item in the scheme should be given away first.
* Each item in a receipt can only be used once, one item cannot be used for both a stamp and a payment, nor can an item be used in multiple schemes.  