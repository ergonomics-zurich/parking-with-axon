# parking-with-axon
A parking backend implemented with Axon

The application manages free parking space in so called "off-street" parking,
i.e. garages.
It expects customers to identify themselves using an NFC card at the entry
and exit barriers.
Each card has an associated balance from which the parking tickets are debited
when exiting the parking.

## Usage

### Cards
```text
GET /cards/{uid}
GET /cards/{uid}/updates (SSE)
POST /cards/issue
POST /cards/{uid}/credit/{amount}
POST /cards/{uid}/debit/{amount}
```

### Garages
```text
GET /garages
GET /garages/best
```

### Backoffice
```text
GET /backoffice/liabilities
GET /backoffice/ongoing-tickets
```
