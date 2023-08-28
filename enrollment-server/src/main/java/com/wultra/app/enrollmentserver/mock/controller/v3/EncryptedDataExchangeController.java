/*
 * PowerAuth integration libraries for RESTful API applications, examples and
 * related software components
 *
 * Copyright (C) 2018 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wultra.app.enrollmentserver.mock.controller.v3;

import com.wultra.app.enrollmentserver.mock.model.request.DataExchangeRequest;
import com.wultra.app.enrollmentserver.mock.model.response.DataExchangeResponse;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.rest.api.spring.annotation.EncryptedRequestBody;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuth;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuthEncryption;
import io.getlime.security.powerauth.rest.api.spring.authentication.PowerAuthApiAuthentication;
import io.getlime.security.powerauth.rest.api.spring.encryption.EncryptionContext;
import io.getlime.security.powerauth.rest.api.spring.encryption.EncryptionScope;
import io.getlime.security.powerauth.rest.api.spring.exception.PowerAuthAuthenticationException;
import io.getlime.security.powerauth.rest.api.spring.exception.PowerAuthEncryptionException;
import io.getlime.security.powerauth.rest.api.spring.exception.authentication.PowerAuthSignatureInvalidException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sample end-point demonstrating how to receive and send encrypted data.
 *
 * <p><b>PowerAuth protocol versions:</b>
 * <ul>
 *     <li>3.0</li>
 * </ul>
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController("encryptedDataExchangeControllerV3")
@RequestMapping(value = "/exchange")
@Slf4j
public class EncryptedDataExchangeController {

    /**
     * Sample encrypted data exchange in application scope.
     *
     * @param request Data exchange request.
     * @param encryptionContext Encryption context.
     * @return Data exchange response.
     * @throws PowerAuthEncryptionException In case encryption or decryption fails.
     */
    @RequestMapping(value = "v3/application", method = RequestMethod.POST)
    @PowerAuthEncryption(scope = EncryptionScope.APPLICATION_SCOPE)
    public DataExchangeResponse exchangeInApplicationScope(@EncryptedRequestBody DataExchangeRequest request,
                                             EncryptionContext encryptionContext) throws PowerAuthEncryptionException {

        if (encryptionContext == null) {
            logger.error("Encryption failed");
            throw new PowerAuthEncryptionException();
        }

        // Return a slightly different String containing original data in response
        return new DataExchangeResponse("Server successfully decrypted signed data: " + (request == null ? "''" : request.getData()) + ", scope: " + encryptionContext.getEncryptionScope());
    }

    /**
     * Sample encrypted data exchange in activation scope.
     *
     * @param request Data exchange request.
     * @param encryptionContext Encryption context.
     * @return Data exchange response.
     * @throws PowerAuthEncryptionException In case encryption or decryption fails.
     */
    @RequestMapping(value = "v3/activation", method = RequestMethod.POST)
    @PowerAuthEncryption(scope = EncryptionScope.ACTIVATION_SCOPE)
    public DataExchangeResponse exchangeInActivationScope(@EncryptedRequestBody DataExchangeRequest request,
                                            EncryptionContext encryptionContext) throws PowerAuthEncryptionException {

        if (encryptionContext == null) {
            logger.error("Encryption failed");
            throw new PowerAuthEncryptionException();
        }

        // Return a slightly different String containing original data in response
        return new DataExchangeResponse("Server successfully decrypted signed data: " + (request == null ? "''" : request.getData()) + ", scope: " + encryptionContext.getEncryptionScope());
    }

    /**
     * Sample signed and encrypted data exchange.
     *
     * @param request Data exchange request.
     * @param encryptionContext Encryption context.
     * @param auth PowerAuth authentication object.
     * @return Data exchange response.
     * @throws PowerAuthAuthenticationException In case signature validation fails.
     * @throws PowerAuthEncryptionException In case encryption or decryption fails.
     */
    @RequestMapping(value = "v3/signed", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/exchange/v3/signed")
    @PowerAuthEncryption(scope = EncryptionScope.ACTIVATION_SCOPE)
    public DataExchangeResponse exchangeSignedAndEncryptedData(@EncryptedRequestBody DataExchangeRequest request,
                                                                EncryptionContext encryptionContext,
                                                                PowerAuthApiAuthentication auth) throws PowerAuthAuthenticationException, PowerAuthEncryptionException {

        if (auth == null || auth.getUserId() == null) {
            logger.info("Signature validation failed");
            throw new PowerAuthSignatureInvalidException();
        }

        if (encryptionContext == null) {
            logger.error("Encryption failed");
            throw new PowerAuthEncryptionException();
        }

        // Return a slightly different String containing original data in response
        return new DataExchangeResponse("Server successfully decrypted data and verified signature, request data: " + (request == null ? "''" : request.getData()) + ", user ID: " + auth.getUserId());
    }

    /**
     * Sample signed and encrypted data exchange of String data.
     *
     * @param requestData Request with String data.
     * @param encryptionContext Encryption context.
     * @param auth PowerAuth authentication object.
     * @return Data exchange response.
     * @throws PowerAuthAuthenticationException In case signature validation fails.
     * @throws PowerAuthEncryptionException In case encryption or decryption fails.
     */
    @RequestMapping(value = "v3/signed/string", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/exchange/v3/signed/string")
    @PowerAuthEncryption(scope = EncryptionScope.ACTIVATION_SCOPE)
    public String exchangeSignedAndEncryptedDataString(@EncryptedRequestBody String requestData,
                                                                       EncryptionContext encryptionContext,
                                                                       PowerAuthApiAuthentication auth) throws PowerAuthAuthenticationException, PowerAuthEncryptionException {

        if (auth == null || auth.getUserId() == null) {
            logger.info("Signature validation failed");
            throw new PowerAuthSignatureInvalidException();
        }

        if (encryptionContext == null) {
            logger.error("Encryption failed");
            throw new PowerAuthEncryptionException();
        }

        // Return a slightly different String containing original data in response
        return "Server successfully decrypted data and verified signature, request data: " + requestData + ", user ID: " + auth.getUserId();
    }

    /**
     * Sample signed and encrypted data exchange of raw data as byte[].
     *
     * @param requestData Request with raw byte[] data.
     * @param encryptionContext Encryption context.
     * @param auth PowerAuth authentication object.
     * @return Data exchange response.
     * @throws PowerAuthAuthenticationException In case signature validation fails.
     * @throws PowerAuthEncryptionException In case encryption or decryption fails.
     */
    @RequestMapping(value = "v3/signed/raw", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/exchange/v3/signed/raw")
    @PowerAuthEncryption(scope = EncryptionScope.ACTIVATION_SCOPE)
    public byte[] exchangeSignedAndEncryptedDataRaw(@EncryptedRequestBody byte[] requestData,
                                                               EncryptionContext encryptionContext,
                                                               PowerAuthApiAuthentication auth) throws PowerAuthAuthenticationException, PowerAuthEncryptionException {

        if (auth == null || auth.getUserId() == null) {
            logger.info("Signature validation failed");
            throw new PowerAuthSignatureInvalidException();
        }

        if (encryptionContext == null) {
            logger.error("Encryption failed");
            throw new PowerAuthEncryptionException();
        }

        // Return data back for verification
        return requestData;
    }

    /**
     * Sample signed and encrypted data exchange of generified request.
     *
     * @param request Request with generified request data.
     * @param encryptionContext Encryption context.
     * @param auth PowerAuth authentication object.
     * @return Generified data exchange response.
     * @throws PowerAuthAuthenticationException In case signature validation fails.
     * @throws PowerAuthEncryptionException In case encryption or decryption fails.
     */
    @RequestMapping(value = "v3/signed/generics", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/exchange/v3/signed/generics")
    @PowerAuthEncryption(scope = EncryptionScope.ACTIVATION_SCOPE)
    public ObjectResponse<DataExchangeResponse> exchangeSignedAndEncryptedDataGenerics(@EncryptedRequestBody ObjectRequest<DataExchangeRequest> request,
                                                                                       EncryptionContext encryptionContext,
                                                                                       PowerAuthApiAuthentication auth) throws PowerAuthAuthenticationException, PowerAuthEncryptionException {
        if (auth == null || auth.getUserId() == null) {
            logger.info("Signature validation failed");
            throw new PowerAuthSignatureInvalidException();
        }

        if (encryptionContext == null) {
            logger.error("Encryption failed");
            throw new PowerAuthEncryptionException();
        }

        if (request == null) {
            logger.error("Missing request");
            throw new PowerAuthEncryptionException();
        }

        if (request.getRequestObject() == null) {
            logger.error("Invalid request");
            throw new PowerAuthEncryptionException();
        }

        // Return generified data back for verification
        DataExchangeResponse response = new DataExchangeResponse();
        response.setData(request.getRequestObject().getData());
        return new ObjectResponse<>(response);
    }

}
