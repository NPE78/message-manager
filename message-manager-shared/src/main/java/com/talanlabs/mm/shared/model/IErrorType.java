package com.talanlabs.mm.shared.model;

import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
import java.io.Serializable;

/**
 * Any error type must implement this interface.<br>
 * With the message-manager-engine module, it is added to a dictionary using SubDictionary#defineError
 */
public interface IErrorType extends Serializable {

    /**
     * The code of the error (unique for a message type)
     */
    String getCode();

    /**
     * The recycling kind induced by the error
     */
    ErrorRecyclingKind getRecyclingKind();

    /**
     * The message will be recycled after this duration (in minutes), only if the message has to be recycled automatically
     */
    Integer getNextRecyclingDuration();

    static IErrorType of(String code, ErrorRecyclingKind errorRecyclingKind, Integer nextRecyclingDuration) {
        return new IErrorType() {
            @Override
            public String getCode() {
                return code;
            }

            @Override
            public ErrorRecyclingKind getRecyclingKind() {
                return errorRecyclingKind;
            }

            @Override
            public Integer getNextRecyclingDuration() {
                return nextRecyclingDuration;
            }
        };
    }

}
