// MongoDB init script - nightflow_events veritabanı ve kullanıcı oluşturma
db = db.getSiblingDB('nightflow_events');

db.createUser({
   user: 'nightflow',
   pwd: 'nightflow123',
   roles: [
      { role: 'readWrite', db: 'nightflow_events' }
   ]
});

// Collection oluştur (opsiyonel)
db.createCollection('events');

print('nightflow_events database and user created successfully!');
