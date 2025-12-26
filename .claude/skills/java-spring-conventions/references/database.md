# Database Management with Flyway

## Schema Versioning with Flyway

### Purpose

Flyway manages database schema migrations:
- Version control for database schema
- Reproducible deployments
- Track all schema changes
- Rollback to previous versions

### Key Principles

**Migration-First**:
- All schema changes via versioned migrations
- Never modify database manually
- Every migration is immutable once applied

**Forward-Only Migrations**:
- Migrations only apply forward
- Old migrations never change
- New migrations for any schema change

## Migration File Structure

### File Naming

**Convention**: `V<version>__<description>.sql`
- V: Version prefix (uppercase V)
- Version: Numeric (V1, V2, V3, etc.)
- Double underscore: Separator
- Description: Snake_case, descriptive

**Examples**:
- V1__initial_schema.sql
- V2__create_orders_table.sql
- V3__add_user_phone_column.sql
- V4__create_order_items_table.sql

### Code Example - Migration Files

**V1__initial_schema.sql**:
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    total_price BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
```

**V2__add_user_phone_column.sql**:
```sql
ALTER TABLE users
ADD COLUMN phone VARCHAR(20);

ALTER TABLE users
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true;

CREATE INDEX idx_users_email ON users(email);
```

**V3__create_order_items_table.sql**:
```sql
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
```

**V4__add_discount_columns.sql**:
```sql
ALTER TABLE orders
ADD COLUMN discount_amount BIGINT DEFAULT 0;

ALTER TABLE orders
ADD COLUMN discount_percent DOUBLE;

ALTER TABLE users
ADD COLUMN loyalty_points INT DEFAULT 0;
```

### Migration Directory

**Default Location**: `db/migration/`
- Flyway looks in this directory
- Configuration can change location
- Keep migrations organized

## Writing Migrations

### Creating Tables

**Migration Contents**:
- CREATE TABLE statements
- Column definitions (type, constraints)
- Primary keys
- Foreign key constraints (if needed)
- Indexes

### Modifying Tables

**Common Modifications**:
- Adding columns: ALTER TABLE ADD COLUMN
- Removing columns: ALTER TABLE DROP COLUMN
- Changing type: ALTER TABLE MODIFY COLUMN
- Adding constraints: ALTER TABLE ADD CONSTRAINT
- Creating indexes: CREATE INDEX

### Data Changes

**Data Migrations** (when needed):
- INSERT statements for initial data
- UPDATE for data transformation
- DELETE for cleanup
- Use sparingly, prefer code changes

## Best Practices

### Migration Design

1. **Small Changes**: One logical change per migration
2. **Reversibility**: Migrations should be reversible (if needed)
3. **Idempotent**: Running twice should be safe
4. **Testable**: Test migrations before deployment

### Schema Design

**Naming**:
- Table names: lowercase, plural (orders, users)
- Column names: lowercase, descriptive
- Constraint names: Clear naming convention

**Primary Keys**:
- Every table needs primary key
- Usually numeric ID (BIGINT)
- Auto-increment when possible

**Foreign Keys**:
- Store as ID references (not object relationships)
- Create indexes on foreign key columns
- Optional: Constraints for referential integrity

### Null Handling

**Default Values**:
- Set NOT NULL with sensible defaults
- Or allow NULL for optional fields
- Be explicit

**Constraints**:
- Use NOT NULL for required fields
- Use NULL for optional fields
- Document why field is nullable

## Migration Workflow

### Development

1. Design schema change needed
2. Create migration file with next version
3. Write SQL to make change
4. Test locally
5. Commit migration file

### Deployment

1. Migration file already committed
2. Deploy application
3. Flyway applies new migrations
4. Database now in new state
5. Application uses new schema

### Rollback

**Downgrade (if needed)**:
- Usually not done (migrations forward only)
- Create new migration to undo changes
- Or restore from backup

## Troubleshooting

### Migration Failures

**If migration fails**:
- Check syntax
- Verify database state
- Resolve issue and update migration
- Or create compensating migration

**Flyway State**:
- Failed migrations marked in history
- Manual intervention may be needed
- Clean and rerun in development
- Careful in production

### Data Loss Prevention

**Backups**:
- Always backup before major migrations
- Test migrations in development first
- Have rollback plan

**Validation**:
- Check data before and after
- Test application with new schema
- Verify indexes created for performance

## Integration with Application

### Springboot Auto-Configuration

**Default Behavior**:
- Flyway automatically runs on startup
- Applies pending migrations
- Fails startup if migration fails

**Configuration**:
- Migration locations
- Baseline version (if existing database)
- Validation settings

### Testing with Migrations

**Test Database**:
- Use embedded database (H2, etc.)
- Flyway applies migrations automatically
- Test with production-like schema

**Integration Tests**:
- Database state between tests
- Transactions for test isolation
- Clean state for each test
