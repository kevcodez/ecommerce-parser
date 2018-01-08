This project aims at providing a simple parser for the top 100 e commerce websites. This project is in an extremely early stage.

[![Build Status](https://travis-ci.org/kevcodez/ecommerce-parser.svg?branch=master)](https://travis-ci.org/kevcodez/ecommerce-parser)
[![Coverage Status](https://coveralls.io/repos/github/kevcodez/ecommerce-parser/badge.svg)](https://coveralls.io/github/kevcodez/ecommerce-parser)

# Usage

Using the parser is as simple as it gets:

```java
class Test {
    
    public static void main(String[] args) {
        WebsiteSourceDownloader websiteSourceDownloader = new JsoupSourceDownloader();
        ECommerceParser eCommerceParser = new ECommerceParser(websiteSourceDownloader);
        
        Product product = eCommerceParser.parseLink("https://www.alternate.de/html/product/1289011"); 
    }
}
```

The product contains information about a product such as the title, description, current price, discount and image variants.

```json
{
  "url": "https:\/\/www.alternate.de\/html\/product\/1289011",
  "externalId": "1289011",
  "title": "Crucial MX300 525 GB, Solid State Drive",
  "description": "Mit der Crucial MX300 525 GB 2,5\" SSD verbessert man die Systemleistung des PC oder Notebook. In wenigen Sekunden ist der Rechner nach dem Start betriebsbereit. Die MX300 525 GB erreicht Lesegeschwindigkeiten von 530 MB\/S und Schreibgeschwindigkeiten von bis zu 510 MB\/s. Dank ihres geringen Energieverbrauchs verhindert sie ein zus\u00e4tzliches Erw\u00e4rmen des Systems.",
  "price": {
    "currentPrice": 132.9,
    "currency": "EUR",
    "discount": {
      "value": 2,
      "percentage": 1
    }
  },
  "images": [
    {
      "variants": [
        {
          "url": "https:\/\/www.alternate.de\/p\/230x230\/h\/AMD_Ryzen_5_1400_WRAITH__Prozessor@@imkmcs0.jpg",
          "height": 230,
          "width": 230
        },
        {
          "url": "https:\/\/www.alternate.de\/p\/50x50\/h\/AMD_Ryzen_5_1400_WRAITH__Prozessor@@imkmcs0.jpg",
          "height": 50,
          "width": 50
        },
        {
          "url": "https:\/\/www.alternate.de\/p\/o\/h\/AMD_Ryzen_5_1400_WRAITH__Prozessor@@imkmcs0.jpg",
          "height": 50,
          "width": 50
        }
      ]
    }
  ]
}
```

# Currently supported

* Amazon.de, Amazon.com
* Alternate.de
* Bonprix.de
* Conrad.de, Conrad.it

Partially: 

* Cyberport.de (Images missing)

# Development

This project is using Java 8 with Maven, Junit 5, Mockito 2. [Jsoup](https://github.com/jhy/jsoup) is used for parsing.

Build the project using `mvn clean install`.

## Writing a new parser

To write a new parser simply extend the `AbstractProductParser` - see **de.kevcodez.ecommerce.parser.impl** package.
The class is pretty straight-forward to implement.

For now, the parser needs to explicitly be registered at the `ECommerceParser` class. This might change to a more dynamic approach later.

# Motivation

The motivation is due to a private project (cannot tell too much for now).

I've had quite a few issues with official APIs such as the Amazon Product Advertising API which throttles the requests very harshly.
Thus, I did it the good ol' way by scraping the page source.