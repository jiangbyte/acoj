package banner

import (
	"context"

	"hei-gin/core/db"
	"hei-gin/core/pojo"
	"hei-gin/ent/gen"
	"hei-gin/ent/gen/sysbanner"
)

type Dao struct{}

func (d *Dao) FindByID(ctx context.Context, id string) (*gen.SysBanner, error) {
	return db.Client.SysBanner.Query().Where(sysbanner.IDEQ(id)).Only(ctx)
}

func (d *Dao) FindPage(ctx context.Context, bounds *pojo.PageBounds) ([]*gen.SysBanner, int, error) {
	total, err := db.Client.SysBanner.Query().Count(ctx)
	if err != nil {
		return nil, 0, err
	}
	records, err := db.Client.SysBanner.Query().Offset(bounds.Offset()).Limit(bounds.Size).All(ctx)
	if err != nil {
		return nil, 0, err
	}
	return records, total, nil
}

func (d *Dao) DeleteByIDs(ctx context.Context, ids []string) error {
	_, err := db.Client.SysBanner.Delete().Where(sysbanner.IDIn(ids...)).Exec(ctx)
	return err
}
