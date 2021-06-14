package simulator

import breeze.linalg._
import scala.io.Source

object DataNew {
val pics = DenseMatrix(
    List(-142, -138, -140, -150, -154, -144, -136, -140, -150, -154, -144, -132, -136, -132, -136, -130, -128, -134, -138, -140, -130, -128, -125, -134, -123, -117, -119, -136, -150, -154, -144, -136, -138, -150, -154, -144, -138, -140, -150, -152, -144, -136, -140, -150, -154, -144, -138, -140, -150),
    List(-196, -180, -174, -180, -194, -196, -180, -176, -180, -194, -198, -117, -119, -107, -105, -105, -95, -97, -91, -97, -93, -95, -99, -111, -111, -113, -117, -146, -178, -190, -196, -184, -176, -180, -196, -198, -182, -176, -180, -194, -196, -182, -176, -178, -194, -198, -182, -174, -180),
    List(-210, -202, -198, -202, -212, -210, -200, -196, -202, -210, -214, -128, -140, -138, -138, -134, -119, -113, -107, -119, -117, -119, -132, -140, -134, -128, -150, -174, -202, -206, -210, -200, -198, -200, -212, -208, -200, -196, -202, -210, -212, -200, -198, -202, -210, -214, -202, -198, -202),
    List(-230, -234, -230, -226, -228, -230, -230, -230, -224, -226, -230, -154, -113, -113, -117, -117, -107, -87, -77, -99, -111, -121, -113, -107, -109, -117, -144, -214, -224, -232, -230, -232, -226, -220, -228, -226, -230, -228, -224, -224, -230, -234, -232, -226, -224, -228, -234, -228, -224),
    List(-230, -232, -236, -236, -234, -230, -232, -236, -236, -234, -234, -150, -119, -123, -134, -132, -107, -71, -55, -81, -103, -128, -128, -130, -128, -134, -152, -230, -236, -236, -228, -230, -226, -232, -230, -228, -230, -234, -234, -230, -232, -236, -232, -234, -232, -230, -236, -234, -236),
    List(-246, -248, -250, -248, -246, -246, -248, -248, -250, -248, -250, -186, -202, -208, -202, -196, -196, -136, -101, -91, -95, -107, -115, -117, -117, -125, -158, -230, -238, -238, -246, -238, -240, -244, -242, -242, -248, -242, -244, -244, -246, -244, -250, -248, -250, -250, -248, -248, -248),
    List(-248, -248, -250, -248, -248, -248, -248, -248, -248, -252, -250, -216, -206, -218, -222, -212, -214, -206, -105, -63, -81, -93, -77, -73, -73, -107, -166, -224, -234, -228, -240, -242, -246, -248, -238, -242, -248, -248, -248, -250, -246, -244, -248, -248, -250, -250, -250, -246, -248),
    List(-254, -254, -254, -254, -254, -254, -252, -254, -254, -254, -254, -234, -212, -222, -224, -220, -212, -190, -140, -107, -61, -41, -55, -71, -85, -132, -158, -212, -226, -238, -242, -250, -244, -244, -238, -250, -248, -252, -252, -254, -252, -252, -254, -254, -254, -254, -254, -254, -254),
    List(-256, -254, -256, -256, -256, -254, -256, -254, -254, -256, -254, -252, -236, -238, -236, -244, -236, -200, -148, -132, -71, -69, -81, -85, -99, -154, -174, -222, -234, -232, -252, -236, -238, -236, -246, -252, -254, -252, -252, -252, -254, -254, -254, -256, -256, -256, -256, -256, -254),
    List(-254, -254, -256, -256, -254, -254, -256, -254, -256, -256, -256, -254, -244, -246, -244, -250, -252, -220, -194, -188, -150, -101, -101, -123, -142, -178, -198, -238, -240, -228, -232, -238, -234, -228, -230, -248, -252, -248, -248, -252, -254, -248, -256, -254, -256, -256, -254, -254, -254),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -254, -248, -252, -252, -254, -250, -234, -192, -180, -158, -128, -134, -156, -178, -178, -206, -242, -236, -222, -232, -242, -236, -238, -238, -244, -248, -252, -252, -254, -252, -250, -256, -256, -256, -256, -256, -256, -256),
    List(-254, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -254, -248, -254, -254, -256, -250, -232, -198, -186, -170, -140, -150, -156, -164, -176, -212, -248, -238, -238, -242, -236, -242, -242, -244, -250, -250, -254, -254, -248, -254, -254, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -252, -256, -254, -252, -248, -232, -202, -178, -152, -140, -128, -144, -134, -164, -220, -248, -222, -228, -232, -236, -232, -240, -240, -250, -250, -246, -246, -252, -254, -254, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -254, -254, -254, -254, -248, -228, -186, -172, -130, -107, -99, -99, -109, -130, -196, -220, -202, -224, -224, -230, -220, -234, -240, -250, -250, -242, -242, -250, -252, -254, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -254, -248, -252, -252, -252, -250, -228, -174, -148, -130, -91, -99, -109, -115, -154, -202, -210, -218, -200, -196, -232, -236, -228, -236, -246, -226, -242, -234, -240, -252, -250, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -254, -230, -246, -248, -246, -246, -210, -152, -117, -111, -142, -134, -142, -150, -186, -214, -236, -232, -220, -214, -232, -234, -216, -228, -232, -230, -240, -232, -244, -254, -248, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -254, -252, -238, -174, -204, -228, -198, -200, -174, -111, -91, -107, -140, -156, -164, -180, -198, -238, -236, -208, -230, -232, -214, -228, -218, -232, -236, -234, -232, -230, -246, -248, -252, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -254, -246, -220, -158, -178, -200, -180, -168, -158, -103, -105, -117, -134, -146, -158, -168, -190, -228, -236, -210, -226, -226, -222, -214, -222, -222, -228, -228, -230, -244, -238, -244, -250, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -254, -250, -240, -188, -198, -218, -206, -196, -168, -107, -95, -103, -136, -142, -146, -152, -166, -216, -216, -216, -210, -214, -216, -200, -220, -234, -216, -226, -232, -236, -230, -238, -250, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -254, -252, -230, -174, -182, -200, -184, -170, -158, -130, -121, -128, -140, -148, -148, -158, -174, -234, -232, -210, -216, -216, -220, -206, -212, -224, -220, -216, -226, -224, -238, -242, -252, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -254, -234, -186, -190, -210, -196, -178, -172, -170, -184, -172, -170, -186, -194, -206, -220, -244, -242, -218, -212, -212, -216, -218, -212, -208, -220, -224, -216, -236, -244, -244, -254, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -252, -226, -176, -188, -190, -188, -166, -150, -162, -158, -198, -172, -168, -196, -188, -206, -242, -242, -220, -198, -206, -218, -222, -220, -214, -218, -228, -232, -226, -244, -238, -252, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -252, -222, -160, -170, -172, -172, -152, -138, -144, -123, -148, -156, -160, -184, -158, -184, -242, -238, -208, -206, -216, -212, -206, -220, -218, -222, -228, -232, -236, -240, -234, -254, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -250, -212, -218, -228, -218, -208, -180, -186, -156, -150, -172, -180, -184, -194, -218, -252, -230, -216, -218, -222, -222, -228, -224, -226, -224, -226, -226, -244, -244, -252, -256, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -246, -250, -254, -244, -244, -208, -218, -164, -162, -166, -170, -194, -210, -240, -246, -224, -208, -204, -216, -218, -222, -226, -216, -224, -230, -236, -242, -246, -242, -250, -256, -256, -256, -256, -256, -256, -256),
    List(-256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -256, -254, -254, -240, -174, -174, -186, -186, -200, -208, -252, -250, -222, -182, -190, -172, -196, -202, -198, -210, -200, -224, -222, -238, -232, -236, -252, -256, -256, -256, -256, -256, -256, -256)
    )

    val flt1 = DenseVector(
    DenseMatrix((12, 28, -5),(1, -5, -4),(-42, -18, 48)),
    DenseMatrix((5, 26, 11),(-19, 3, -9),(64, 1, -12)),
    DenseMatrix((33, 8, 19),(-6, -4, 2),(9, 38, 11)),
    DenseMatrix((16, 17, -11),(0, -24, -42),(-17, -4, 10)),
    DenseMatrix((-29, 0, 15),(4, -28, 1),(27, -36, -16)),
    DenseMatrix((12, 27, 14),(-1, -2, -12),(-13, -28, -18)),
    DenseMatrix((22, 6, -27),(26, -8, -23),(10, 14, -8)),
    DenseMatrix((1, 7, 39),(17, -13, -23),(16, -24, -20)),
    DenseMatrix((21, -35, -3),(22, -30, -3),(3, -4, 18)),
    DenseMatrix((-26, -16, -14),(-18, 12, 6),(15, 16, -8)),
    DenseMatrix((-3, 34, -3),(7, 1, -27),(11, 0, -28)),
    DenseMatrix((-28, -21, -13),(-12, -4, 11),(-6, 26, 13)),
    DenseMatrix((-9, -2, 9),(-27, 14, -2),(-41, -8, 25)),
    DenseMatrix((-22, 39, -24),(-3, 12, -11),(-12, 13, -8)),
    DenseMatrix((31, -16, -21),(-1, -10, 14),(-13, -25, 16)),
    DenseMatrix((-11, -8, 7),(-7, -13, -13),(-3, -23, -30)),
    DenseMatrix((-23, -16, 26),(-8, -38, 16),(-9, -1, 12)),
    DenseMatrix((-10, -18, -41),(16, 22, -10),(12, 11, -2)),
    DenseMatrix((-5, 2, -12),(39, -9, -8),(27, -7, -30)),
    DenseMatrix((-38, 15, 21),(-28, 8, 9),(-7, -8, 15)),
    DenseMatrix((-22, -22, -23),(8, 4, 6),(-1, 22, 15)),
    DenseMatrix((-12, 7, 23),(-29, -6, 16),(-24, 3, 11)),
    DenseMatrix((-14, -28, -7),(12, 7, 11),(12, 20, 1)),
    DenseMatrix((16, -2, -33),(20, 18, -24),(12, -2, -20)),
    DenseMatrix((2, -13, 20),(1, -25, -6),(19, 14, -25)),
    DenseMatrix((19, 1, -33),(10, 3, -30),(14, 10, -11)),
    DenseMatrix((-17, -41, 0),(-13, -6, 13),(9, 28, 20)),
    DenseMatrix((8, 17, 15),(5, 11, 0),(-28, -26, -10))
    )
}