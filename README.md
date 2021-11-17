# parking-with-axon

A parking backend implemented using
[Axon Framework](https://axoniq.io/product-overview/axon-framework)
and [Axon Server](https://axoniq.io/product-overview/axon-server).

The application currently covers functionality from three contexts:

### Garage Capacity Management

1. It tracks free parking space in _off-street_ parking locations like garages and car parks.
2. It ensures cars are only admitted into a location when free spaces are available.

### Parking Card Management

1. It manages parking cards which carry a balance.
2. It ensures payments are collected whenever a vehicle exits a garage.
3. It ensures the customer keeps the card sufficiently funded.

### Backoffice

1. It shows all ongoing parking permits to customer support.

# Journey

1. The customer approaches a garage's entry barrier. Each garage is identified by a unique name.
2. The customers identify themselves using an NFC card to the entry barrier. The card is identified by a 14 digit hex
   string (7 bytes).
3. The barrier checks whether the garage has free spaces. If free spaces are available, the barrier opens.
4. The entry barrier confirms that the vehicle has entered, e.g. using light beams, and lowers its arm.
5. The customer parks the car and leaves to enjoy the day.
6. The customer returns and drives up to the exit barrier.
7. The customer presents the NFC card to the exit barrier, which attemts to collect payment.
8. If the payment was collected successfully, the barrier raises its arm.
9. The exit barrier confirms that the vehicle has left and finalizes the records.

# Usage

### Cards

```shell
# list all existing card uids
curl -X GET http://localhost:8080/cards
# issue a new parking card
curl -X POST http://localhost:8080/cards
# listen to balance changes (using SSE)
curl -X GET http://localhost:8080/cards/{uid}
# add amount to card balance
curl -X POST http://localhost:8080/cards/{uid}/credit/{amount}
```

### Garages

```shell
# list all existing parking garages
curl -X GET http://localhost:8080/garages
# return the most empty parking garage
curl -X GET http://localhost:8080/garages/best
# register a new garage
curl -X POST http://localhost:8080/garages[?capacity=1&used=0]
```

### Parking

```shell
# check whether the entry barrier should be opened
curl -X POST http://localhost:8080/garages/{gid}/request-entry/{uid}
# register that the vehicle has entered
curl -X POST http://localhost:8080/garages/{gid}/confirm-entry/{uid}
# check whether the exit barrier should be opened
curl -X POST http://localhost:8080/garages/{gid}/request-exit/{uid}
# register that the vehicle has exited
curl -X POST http://localhost:8080/garages/{gid}/confirm-exit/{uid}
```

### Backoffice

```shell
# list permits that are currently active in garages
GET /backoffice/active-permits
```
