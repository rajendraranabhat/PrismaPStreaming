CREATE KEYSPACE prisma WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};

CREATE TABLE prisma.score (
    accountno TEXT,
    pred_cv double,
    cat_cv TEXT,
    pred_mv double,
    cat_mv TEXT,
    pred_icu double,
    cat_icu TEXT,
    pred_30d double,
    cat_30d TEXT,
    PRIMARY KEY(accountno)
);

INSERT INTO prisma.score (accountno, pred_cv, cat_cv, pred_mv, cat_mv, pred_icu,cat_icu, pred_30d, cat_30d)
  VALUES ('A1F39105',0.0645287478,'low',1,'high',0.9999687633,'high',0.9999999997,'high');
INSERT INTO prisma.score (accountno, pred_cv, cat_cv, pred_mv, cat_mv, pred_icu,cat_icu, pred_30d, cat_30d)
  VALUES ('A1F40528',0.0197866678,'low',1,'high',0.9999672861,'high',0.9999999999,'high');
INSERT INTO prisma.score (accountno, pred_cv, cat_cv, pred_mv, cat_mv, pred_icu,cat_icu, pred_30d, cat_30d)
  VALUES ('A1F40983',0.1869652932,'moderate',1,'high',0.9999984865,'high',0.9999999998,'high');
INSERT INTO prisma.score (accountno, pred_cv, cat_cv, pred_mv, cat_mv, pred_icu,cat_icu, pred_30d, cat_30d)
  VALUES ('A1F42085',5.1763572293884E-007,'low',1,'high',0.0031757997,'low',1,'high');
INSERT INTO prisma.score (accountno, pred_cv, cat_cv, pred_mv, cat_mv, pred_icu,cat_icu, pred_30d, cat_30d)
  VALUES ('A1F43620',0.0186892389,'low',1,'high',0.9998459088,'high',1,'high');
INSERT INTO prisma.score (accountno, pred_cv, cat_cv, pred_mv, cat_mv, pred_icu,cat_icu, pred_30d, cat_30d)
  VALUES ('A1H44498',0.0931387121,'low',1,'high',0.9999750573,'high',0.9999999999,'high');
INSERT INTO prisma.score (accountno, pred_cv, cat_cv, pred_mv, cat_mv, pred_icu,cat_icu, pred_30d, cat_30d)
  VALUES ('A1H44957',0.017906782,'low',1,'high',0.9999263084,'high',0.9999999988,'high');
INSERT INTO prisma.score (accountno, pred_cv, cat_cv, pred_mv, cat_mv, pred_icu,cat_icu, pred_30d, cat_30d)
  VALUES ('A1H45041',0.0253865948,'low',1,'high',0.9999814567,'high',0.9999999996,'high');
INSERT INTO prisma.score (accountno, pred_cv, cat_cv, pred_mv, cat_mv, pred_icu,cat_icu, pred_30d, cat_30d)
  VALUES ('A1H46214',0.1959837239,'moderate',1,'high',0.9999982898,'high',0.9999999996,'high');
INSERT INTO prisma.score (accountno, pred_cv, cat_cv, pred_mv, cat_mv, pred_icu,cat_icu, pred_30d, cat_30d)
  VALUES ('A1H46215',0.0036161278,'low',1,'high',0.9998288551,'high',0.9999999997,'high');

  
