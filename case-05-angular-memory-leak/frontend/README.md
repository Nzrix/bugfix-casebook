# Angular Memory Leak Frontend Demo

## Setup

```bash
# Create Angular app (requires Node.js 18+)
npm install -g @angular/cli@17
ng new product-app --routing=true --style=css
cd product-app

# Copy the example files from /examples to /src/app
# - product-list-before.component.ts
# - product-list-after-async.component.ts
# - product-list-after-destroy.component.ts
# - product.service.ts
```

## Running

```bash
# Start backend first (port 8085)
cd ../backend
mvn spring-boot:run

# Start Angular dev server (port 4200)
cd ../frontend/product-app
npm start

# Open http://localhost:4200
```

## Testing Memory Leaks

1. Open Chrome DevTools → Performance/Memory tab
2. Navigate between BEFORE and AFTER components multiple times
3. Take heap snapshots to see memory growth
4. Check Network tab to see duplicate HTTP calls

## Key Files

- `product-list-before.component.ts` - Demonstrates the bug
- `product-list-after-*.component.ts` - Shows fixes
- `product.service.ts` - Service with shareReplay
