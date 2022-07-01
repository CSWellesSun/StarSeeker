from astropy.io import ascii
from astropy.coordinates import EarthLocation, SkyCoord, AltAz
from astropy import units
import imufusion
import numpy


# 根据输入的天体HR编号返回天体的RA与DES(北极星的HR编号是424)
# 返回格式第一个项是RA, 第二个项是DES
def get_ra_and_des(hr_num):
    # 读取文件提取出对应行
    table = ascii.read("catalog", readme="ReadMe")
    table.add_index('HR')
    row = table.loc[hr_num]
    # 提取查询得到的RA和DES
    ret = []
    ret.append(str(row[table.index_column('RAh')])+'h'+str(row[table.index_column('RAm')])+'m'+str(row[table.index_column('RAs')])+'s')
    ret.append(str(row[table.index_column('DEd')])+'d'+str(row[table.index_column('DEm')])+'m'+str(row[table.index_column('DEs')])+'s')
    return ret


# 根据输入天体的RA/DES, 海拔, 经度纬度与时间计算天体的地平坐标系坐标(北极星的HR编号是424)
# 参数格式longitude和latitude均为角度, 东经为正,西经为负
# 返回格式是第一个项是ALT, 第二个项是AZ
def get_skycoord(ra_des, longitude, latitude, height, time):
    # 构建观测者
    observer = EarthLocation(lat=latitude*units.deg, lon=longitude*units.deg, height=height*units.m)
    # 构建目标天体
    opt_target = SkyCoord(ra=ra_des[0], des=ra_des[1], unit=(units.hourangle, units.deg))
    opt_pos = opt_target.transform_to(AltAz(obstime=time, location=observer))
    ret = [str(opt_pos.alt), str(opt_pos.az)]
    return ret


# 根据输入的天体RA/DES, 经度纬度与时间计算天体地平坐标系坐标, 根据输入的传感器数据获得现在的姿态角, 返回值范围[0,255]
def get_diff_level(ra_des, longitude, latitude, height, time, gyro, acc, ahrs=imufusion.Ahrs()):
    opt_altaz = get_skycoord(ra_des, longitude, latitude, height, time)
    ahrs.update_no_magnetometer(gyro, acc, 1 / 100)
    head_euler = ahrs.quaternion.to_euler()
    # head_euler[2] = yaw, head_euler[1] = pitch
    distance = numpy.arccos(numpy.cos(opt_altza[0])*numpy.cos(head_euler[1])*numpy.cos(opt_altaz[1]-head_euler[2])+numpy.sin(opt_altaz[0])*numpy.sin(head_euler[0]))
    return int(abs(distance) / numpy.pi * 255)

