/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.api.MappedScene;
import pl.cyfronet.s4e.api.SearchConverter;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.BadRequestException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.QueryException;
import pl.cyfronet.s4e.license.LicensePermissionEvaluator;
import pl.cyfronet.s4e.search.SearchQueryParams;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.cyfronet.s4e.search.SearchQueryParams.PRODUCT_TYPE;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SceneRepository repository;
    private final ProductRepository productRepository;
    private final SearchConverter converter;
    private final LicensePermissionEvaluator licensePermissionEvaluator;

    public List<MappedScene> getScenesBy(Map<String, Object> params) throws SQLException, QueryException {
        return repository.findAllByParamsMap(params);
    }

    public Long getCountBy(Map<String, Object> params) throws SQLException, QueryException {
        return repository.countAllByParamsMap(params);
    }

    public void checkProductTypePresent(Map<String, Object> params) throws BadRequestException {
        if (params.get(PRODUCT_TYPE) == null) {
            throw new BadRequestException("No product type.");
        }
    }

    public void applyLicenseByProductType(Map<String, Object> params, AppUserDetails userDetails)
            throws NotFoundException {
        params.remove(SearchQueryParams.ACCESS_TYPE);
        val product =
                productRepository.findByName(
                        String.valueOf(params.get(PRODUCT_TYPE)), Product.class)
                        .orElseThrow(() -> new NotFoundException("Product not found"));
        // if user doesnt have proper license throw 403
        if (!licensePermissionEvaluator.allowProductRead(product.getId(), userDetails)) {
            throw new AccessDeniedException("Access Denied");
        }
        val accessType = product.getAccessType();
        if (Product.AccessType.EUMETSAT.equals(accessType)) {
            params.put(SearchQueryParams.ACCESS_TYPE, Product.AccessType.EUMETSAT);
            // if user doesnt have proper license add parameter to get older scenes
            if (userDetails != null
                    && userDetails.getAuthorities().contains(new SimpleGrantedAuthority("LICENSE_EUMETSAT"))) {
                params.remove(SearchQueryParams.ACCESS_TYPE);
            }
        }
    }

    public Map<String, Object> countParseToParamMap(String query) {
        Map<String, Object> result = new HashMap<>();
        if (query != null) {
            result.putAll(converter.convert(query));
        }
        return result;
    }

    public Map<String, Object> parseToParamMap(String rowsSize, String rowStart, String orderby, String query) {
        Map<String, Object> result = new HashMap<>();
        if (query != null) {
            result.putAll(converter.convert(query));
        }
        result.putAll(converter.convertParams(rowsSize, rowStart, orderby));
        return result;
    }
}
