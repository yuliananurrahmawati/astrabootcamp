/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package org.astra.training.webservices.validator;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.commercefacades.product.data.StockData;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.LowStockException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.ProductLowStockException;
import org.astra.training.webservices.stock.CommerceStockFacade;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static de.hybris.platform.commercewebservicescommons.errors.exceptions.LowStockException.NO_STOCK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link DefaultStockValidator}
 */
@UnitTest
public class DefaultStockValidatorTest
{
	private static final String BASE_SITE_ID = "testSite";
	private static final Long ENTRY_NUMBER = 1L;
	private static final String PRODUCT_CODE = "12345";

	@Mock
	private CommerceStockFacade commerceStockFacade;
	@Mock
	private StockData stock;
	private DefaultStockValidator validator;

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		when(commerceStockFacade.isStockSystemEnabled(anyString())).thenReturn(true);
		when(stock.getStockLevelStatus()).thenReturn(StockLevelStatus.INSTOCK);
		when(commerceStockFacade.getStockDataForProductAndBaseSite(PRODUCT_CODE, BASE_SITE_ID)).thenReturn(stock);
		this.validator = new DefaultStockValidator(commerceStockFacade);
	}

	@Test
	public void testValidateWhenNoStock()
	{
		when(commerceStockFacade.getStockDataForProductAndBaseSite(anyString(), anyString())).thenReturn(null);

		validator.validate(BASE_SITE_ID, PRODUCT_CODE, ENTRY_NUMBER);
	}

	@Test
	public void testValidateWhenOutOfStockAndNoEntryNumber()
	{
		when(stock.getStockLevelStatus()).thenReturn(StockLevelStatus.OUTOFSTOCK);

		final ProductLowStockException actualException = assertThrows(ProductLowStockException.class,
				() -> validator.validate(BASE_SITE_ID, PRODUCT_CODE, null));

		assertThat(actualException).hasMessage("Product [%s] cannot be shipped - out of stock online", PRODUCT_CODE)
				.hasFieldOrPropertyWithValue("reason", NO_STOCK).hasFieldOrPropertyWithValue("subject", PRODUCT_CODE);
	}

	@Test
	public void testValidateWhenOutOfStockWithEntryNumber()
	{
		when(stock.getStockLevelStatus()).thenReturn(StockLevelStatus.OUTOFSTOCK);

		final LowStockException actualException = assertThrows(LowStockException.class,
				() -> validator.validate(BASE_SITE_ID, PRODUCT_CODE, ENTRY_NUMBER));

		assertThat(actualException).hasMessage("Product [%s] cannot be shipped - out of stock online", PRODUCT_CODE)
				.hasFieldOrPropertyWithValue("reason", NO_STOCK).hasFieldOrPropertyWithValue("subject", ENTRY_NUMBER.toString());
	}
}
