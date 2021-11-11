# parking-with-axon
A parking backend implemented with Axon

The application manages free parking space in so called "off-street" parking,
i.e. garages.

It expects customers to identify themselves using an NFC card at the entry
and exit barriers.
Each card is identified by a 14 digit hex string (7 bytes).
And card has an associated balance from which the parking tickets are debited
when exiting the parking.

Entry into a parking garage is only allowed if the garage has free space,
and if the card has a positive balance.
Exit is only allowed if the card's balance can pay for the entire ticket.

Both entry and exit are two step processes.
First the entry or exit is authorized,
then the entry or exit is confirmed once the car passes the barrier.


## Usage

### Cards
```text
GET /cards/{uid}                         return the balance of a parking card
GET /cards/{uid}/updates (SSE)           listen to balance changes
POST /cards/issue                        issue a new parking card
POST /cards/{uid}/credit/{amount}        add amount to card balance
POST /cards/{uid}/debit/{amount}         deduct amount from card balance
```

### Garages
```text
GET /garages                             list all existing parking garages
GET /garages/best                        return the most empty parking garage
POST /garages/{gid}/request-entry/{uid}  check whether the entry barrier should be opened
POST /garages/{gid}/confirm-entry/{uid}  register that the vehicle has entered
POST /garages/{gid}/request-exit/{uid}   check whether the exit barrier should be opened
POST /garages/{gid}/confirm-exit/{uid}   register that the vehicle has exited
```

### Backoffice
```text
GET /backoffice/liability                return the total balance on cards
GET /backoffice/open-tickets             list tickets that are currently in a garage
```
