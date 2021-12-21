# Ranales

Ranales 是免费的，但目前测试阶段暂不提供 jar 文件，你可以通过以下步骤自行构建插件。

**Windows 平台**
```shell
gradlew.bat clean build
```

**macOS/Linux 平台**
```shell
./gradlew clean build
```
<br>

***

<br>

# 目录

 - [简介](#Description)
 - [计划](#Plans)
 - [Target 语句](#Kether-Target)
    - [Target Selector 目标选择器](#Target-Selector)
    - [Target Filter 目标过滤器](#Target-Filter)
    - Target Foreach 目标迭代器

<br>

***

<br>

# <strong id="Description"> 简介 </strong>

Ranales 是一款基于 [Kether](https://kether.tabooproject.org/) 语法的**拓展**插件

我的初衷是将其开发成为一款可高度自定义的**技能**插件

并搭配 [Zaphkiel](https://github.com/Bkm016/Zaphkiel) 插件实现更多奇妙功能

但却不只用它来制作技能，我希望它将来能够做到更多的功能

未来我也会添加更多实用有趣的语句或功能

感谢您的支持！


<br>

***

<br>

# <strong id="Plans"> 计划 </strong>

- [ ] entity 语句
    - [x] [Damage](#Entity-Damage)
    - [x] [Type](#Entity-Type)
- [x] [target 语句](#Kether-Target)
    - [x] [Target Selector 目标选择器](#Target-Selector)
    - [x] [Target Filter 目标过滤器](#Target-Filter)
    - [x] Target Foreach 目标迭代器
- [ ] vector 语句

<br>

# <strong id="Kether-Target"> Target 语句 </strong>

主要掌管与目标相关的功能

用于获取目标、筛选目标、遍历目标集合等

目前拥有以下三个子语句

- [Target Selector 目标选择器](#Target-Selector)
- [Target Filter 目标过滤器](#Target-Filter)
- Target Foreach 目标迭代器

## <strong id="Target-Selector"> Target Selector 目标选择器 </strong>

写法

```yaml
target sel {selector} [ {parameters} ]
target select {selector} [ {parameters} ]
target selector {selector} [ {parameters} ]
```

> 在释放技能时往往需要对目标实体进行释放<br>
而目标选择器就是用来选取技能释放的目标实体<br>
主要用于获取单个目标对象或多个目标对象的集合<br>
[单实体选择器](#Single-Selector)返回 Any 对象<br>
[多实体选择器](#Multi-Selector)返回 Collection\<Any\> 集合对象

<br>

不同的选择器在**参数**以及**简写**方面有所区别，详见具体的实体选择器

<br>

### <strong id="Single-Selector"> 单实体选择器 </strong>
| 选择器 (selector) | 简写 | 参数 (parameters) | 备注 |
|--|--|--|--|
| [self](#Selector-Self) | @self | 无 | 选择脚本执行者自身为目标 |
| [player](#Selector-Player) | @player | name | 根据给定的 name 选择指定 Player |

<br>
<hr>

#### <strong id="Selector-Self"> 实体选择器 Self </strong>

选取脚本执行者 sender 自身作为目标对象

```yaml
target sel self
```

以上语句可直接简写为
```yaml
@Self
```

#### <strong id="Selector-Player"> 实体选择器 Player </strong>

```yaml
// 常规用法
target sel player *name

// 简写
@Player *name
```

根据给定的 **name** 选择对应的玩家作为目标

<br>

例子
```yaml
// 选取名为 Lanscarlos 的玩家
target sel player *Lanscarlos

// 以上语句可直接简写为
@Player *Lanscarlos
```

<hr>
<br>

### <strong id="Multi-Selector"> 多实体选择器 </strong>
| 选择器 | 简写 | 参数 | 备注 |
|--|--|--|--|
| [EntitiesInRadius](#Selector-EntitiesInRadius) | eir，@EIR | location，radius | 根据给定的 **location** 坐标选取附近半径 **radius** 内的所有实体 |
| LivingEntitiesInRadius | leir，@LEIR | location，radius | 根据给定的 **location** 坐标选取附近半径 **radius** 内的所有生物 |

<br>
<hr>

#### <strong id="Selector-EntitiesInRadius"> 实体选择器 EntitiesInRadius </strong>

```yaml
// 常规用法
target sel eir *location *radius

// 简写
@EIR *location *radius
```
根据给定的 **location** 坐标选取其附近半径 **radius** 内的所有实体<br>
location除了可以传入坐标对象外，还可以传入实体对象，插件会自动从实体对象中获取其坐标

<br>

例子<br>
使用 location 语句构建坐标对象
```yaml
// 定义坐标对象 并将其赋值给参数 loc
set loc to location *world *0 *0 *0

// 选取坐标 loc 附近 10 格内的所有实体（Entity）
target sel eir &loc *10

此外，语句可直接简写为
@EIR &loc *10
```
当脚本执行者为玩家时<br>
可以传入 @self 实体对象充当坐标<br>
此时坐标即为玩家对象当前的坐标
```yaml
@EIR @self *10
```

<hr>
<br>

## <strong id="Target-Filter"> Target Filter 目标过滤器 </strong>

```yaml
target filter {targets} {filter} {parameters}

参数：
targets 给定的目标集合 Colletion<Any>
filter 给定的过滤器
```
> 通常使用选择器获取到实体集合后<br>
我们需要对里面的一些实体进行筛选<br>
这个时候就需要用到**目标过滤器**来筛选出符合期望的实体<br>

过滤器通常返回过滤后的实体集合 **Colletion\<Any\>**

<br>

### 与目标选择器 Target Selector 连用
过滤器可直接写在选择器后面，极大简化语句
```yaml
// 常规写法 （与 EntitiesInRadius 选择器连用）
target sel eir &loc *10 filter {filter} {parameters}

// 将选择器 EntitiesInRadius 简化后写法
@EIR &loc *10 filter {filter} {parameters}
```

### 过滤器的链式调用
过滤器可支持多个过滤器的**链式调用**，极大简化语句<br>

> 若您在使用过程中需要用到多个过滤器<br>
我们**建议**您使用链式调用来简化您的语句

```yaml
// 常规写法
target filter {targets} {filter} {parameters} [ filter {filter} {parameters} ]...
```
与选择器 EntitiesInRadius 连用**简化**后写法
```yaml
@EIR &loc *10 filter {filter} {parameters} [ filter {filter} {parameters} ]...
```

下面以 [instance](#Filter-Instance)、[type](#Filter-Type) 过滤器为例 进行**链式调用**<br>
先筛选所有属于 [Animals](https://bukkit.windit.net/javadoc/index.html?org/bukkit/entity/Animals.html) 接口的实体，接着筛选所有实体类型为 cow 与 pig 的实体
```yaml
// 先获取目标集合并赋值于参数 targets
set targets to @EIR &loc *10

// 常规写法
target filter &targets instance *animals filter type [ *cow *pig ]
```
与选择器 EntitiesInRadius 连用**简化**后写法
```yaml
@EIR &loc *10 filter instance *animals filter type [ *cow *pig ]
```

<br>
<hr>


| 过滤器 (filter) | 简写 | 参数 (parameters) | 备注 |
|--|--|--|--|
| [foreach](#Filter-Foreach) | none | condition | 遍历每一个实体并筛选出符合 **condition** 条件的实体 |
| [instance](#Filter-Instance) | inst | string | 根据给定的 类 过滤对应实体 |
| [type](#Filter-Type) | 无 | array | 根据给定的 tpye 类型过滤实体 |

<br>

### <strong id="Filter-Foreach"> 目标过滤器 Foreach </strong>

```yaml
// 常规用法
target filter {targets} foreach *condition
target filter {targets} foreach by <key> *condition

// 与选择器连用写法
@EIR @self *10 filter foreach *condition
@EIR @self *10 filter foreach by <key> *condition
```
根据给定的 **condition** 条件遍历 **targets** 所有实体并筛选出符合条件的实体<br>
在 **condition** 中，默认 **it** 参数为当前遍历的实体 <br>
当然你也可通过 **by \<key\>** 语句来定义参数 **key** 为当前遍历的实体的参数

<br>

例子
与选择器连用，遍历每一个实体
```yaml
// 定义坐标并将其赋值给参数 loc
set loc to location *world *0 *0 *0

// 遍历每一个实体, 将所有 生命值>=10 的实体过滤出来
@EIR &loc *10 filter foreach {

    // 默认 it 参数为当前遍历的个体
    check entity &it health >= *10
}
```

定义 **el** 参数为当前遍历的个体
```yaml
@EIR &loc *10 filter foreach by el {
    check entity &el health >= *10
}
```

### <strong id="Filter-Instance"> 目标过滤器 Instance </strong>

```yaml
// 常规用法
target filter {targets} inst *name
```
根据给定的 **name** 类/接口来遍历 **targets** 中的所有实体并筛选出继承此 **类/接口** 的实体<br>
注意！这里的 name 是指程序包 **[org.bukkit.entity](https://bukkit.windit.net/javadoc/index.html?org/bukkit/entity/package-summary.html)** 下实体对应的 **类/接口** 的名字<br>
大小写均可

<br>

例子<br>
将所有属于 Animals 的实体筛选出来
```yaml
@EIR &loc *10 filter inst *animals
```

### <strong id="Filter-Instance"> 目标过滤器 Type </strong>

```yaml
// 常规用法
target filter {targets} type [ *type1 *type2... ]
```
根据给定的 **type** 筛选 **targets** 中的所有符合给定的类型的实体<br>
注意！这里的 type 是指 **[实体的类型 EntityType](https://bukkit.windit.net/javadoc/index.html?org/bukkit/entity/EntityType.html)**

<br>

例子<br>
将所有实体为 chicken 跟 cow 的实体筛选出来
```yaml
@EIR &loc *10 filter inst *animals
```


<!-- ```yaml
target sel self filter type [ *zombie *husk ]
target sel self filter foreach el by {
    check entity &el health >= *10
}
``` -->