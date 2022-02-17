Acceptance Test Views
=====================
Here are all the AcceptanceTests 

Unknown Layouts
---------------
I haven't looked into these layouts at all. They're probably important.

```
android.widget.CheckBox
android.widget.Spinner
android.widget.Switch
android.widget.TableLayout
android.widget.TableRow
android.widget.ListView
android.widget.ScrollView
android.support.v4.view.ViewPager
android.support.v4.widget.DrawerLayout
```

Dummy Layouts
-------------
We could probably replace these with a DummyView or DummyViewGroup.

```
android.view.View
android.view.ViewGroup
```

Compatibility Layouts
---------------------
I think these can be turned into LinearLayouts.

```
android.support.v7.widget.LinearLayoutCompat
android.widget.TabHost
android.widget.TabWidget
```

Deletable Layouts
-----------------
You can remove these from the tree during XMLParser

```
android.widget.FrameLayout
```

Supported Layouts
-----------------
We already support these.

```
android.widget.Button
android.widget.EditText
android.widget.ImageButton
android.widget.ImageView
android.widget.LinearLayout
android.widget.RelativeLayout
```
