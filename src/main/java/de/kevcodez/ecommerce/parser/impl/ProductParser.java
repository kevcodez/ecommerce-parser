package de.kevcodez.ecommerce.parser.impl;

import de.kevcodez.ecommerce.parser.domain.product.Product;

public interface ProductParser {

    Product parse(String url);

}
