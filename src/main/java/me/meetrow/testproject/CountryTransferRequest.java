package me.meetrow.testproject;

import java.util.UUID;

public record CountryTransferRequest(String countryKey, UUID currentOwnerId, UUID targetPlayerId) {
}
