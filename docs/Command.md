sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --outfile models.py


sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --tables sys_user,sys_userext --outfile /modules/sys/user/models.py

mkdir modules\client\user
sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --tables client_user --outfile ./modules/client/user/models.py

mkdir modules\client\relation
sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --tables client_relation --outfile ./modules/client/relation/models.py

mkdir modules\sys\user
sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --tables sys_user --outfile ./modules/sys/user/models.py

mkdir modules\sys\role
sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --tables sys_role --outfile ./modules/sys/role/models.py

mkdir modules\sys\group
sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --tables sys_group --outfile ./modules/sys/group/models.py

mkdir modules\sys\org
sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --tables sys_org --outfile ./modules/sys/org/models.py

mkdir modules\sys\position
sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --tables sys_position --outfile ./modules/sys/position/models.py

mkdir modules\sys\relation
sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --tables sys_relation --outfile ./modules/sys/relation/models.py

mkdir modules\sys\resource
sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --tables sys_resource,sys_module --outfile ./modules/sys/resource/models.py

mkdir modules\sys\dict
sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --tables sys_dict --outfile ./modules/sys/dict/models.py

mkdir modules\sys\notice
sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --tables sys_notice --outfile ./modules/sys/notice/models.py

mkdir modules\sys\banner
sqlacodegen mysql+pymysql://root:123456@localhost/hei_data --tables sys_banner --outfile ./modules/sys/banner/models.py
