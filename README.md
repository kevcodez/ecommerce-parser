This project aims at providing a simple parser for the top 100 e commerce websites. This project is in an extremely early stage.

Currently supported

* Amazon.de
* Alternate.de

Using the parser is as simple as it gets:

```java
class Test {
    
    public static void main(String[] args) {
        Product product = ECommerceParser.INSTANCE.parseLink("https://www.alternate.de/html/product/1289011"); 
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