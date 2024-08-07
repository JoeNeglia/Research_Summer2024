/*
 * Copyright 2017 Piruin Panichphol
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package me.piruin.spinney.sample;

import android.app.Application;
import me.piruin.spinney.Spinney;

public class SampleApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();

    Spinney.setDefaultItemPresenter(new Spinney.ItemPresenter() {
      @Override public String getLabelOf(Object item, int position) {
        if (item instanceof DatabaseItem)
          return ((DatabaseItem) item).getName();
        else
          return String.format("%s", item.toString());
      }
    });
    //Spinney.enableSafeModeByDefault(true);
  }
}


package me.piruin.spinney.sample;

class DatabaseItem {

  private final int id;
  private final String name;
  private int parentId;

  public DatabaseItem(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public DatabaseItem(int id, String name, int parentId) {
    this(id, name);
    this.parentId = parentId;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getParentId() {
    return parentId;
  }

  @Override public String toString() {
    return "DatabaseItem{" +
      "id=" + id +
      ", name='" + name + '\'' +
      '}';
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DatabaseItem that = (DatabaseItem) o;

    if (id != that.id) return false;
    if (parentId != that.parentId) return false;
    return name.equals(that.name);
  }

  @Override public int hashCode() {
    int result = id;
    result = 31 * result + name.hashCode();
    result = 31 * result + parentId;
    return result;
  }
}


package me.piruin.spinney.sample;

import java.util.Arrays;
import java.util.List;

class Data {

  static final List<String> department =
    Arrays.asList("NSTDA", "NECTEC", "BIOTEC", "MTEC", "NANOTEC");

  static final List<DatabaseItem> country = Arrays.asList(
    new DatabaseItem(1, "THAILAND"),
    new DatabaseItem(2, "JAPAN"),
    new DatabaseItem(3, "SOUTH KOREA"),
    new DatabaseItem(4, "VIETNAM")
  );

  static final List<DatabaseItem> regions = Arrays.asList(
    new DatabaseItem(1, "Center", 1),
    new DatabaseItem(2, "North", 1),
    new DatabaseItem(3, "East-North", 1),
    new DatabaseItem(4, "East", 1),
    new DatabaseItem(5, "West", 1),
    new DatabaseItem(6, "South", 1),
    new DatabaseItem(7, "Hokkaido", 2),
    new DatabaseItem(8, "Kyushu", 2),
    new DatabaseItem(8, "Central", 2)
  );

  static final List<DatabaseItem> cities = Arrays.asList(
    new DatabaseItem(1, "BANGKOK", 1),
    new DatabaseItem(2, "CHONBURI", 1),
    new DatabaseItem(3, "CHIANG MAI", 1),
    new DatabaseItem(4, "TOKYO", 2),
    new DatabaseItem(5, "HOKKAIDO", 2),
    new DatabaseItem(6, "SEOUL", 3),
    new DatabaseItem(7, "HOJIMIN", 4)
  );

  static final List<DatabaseItem> districts = Arrays.asList(
    new DatabaseItem(1, "Bang Khen", 1),
    new DatabaseItem(2, "Chatuchak", 1),
    new DatabaseItem(3, "Don Mueang", 1),
    new DatabaseItem(4, "Pattaya ", 2),
    new DatabaseItem(5, "Doi Tao ", 3)
  );

}


/*
 * Copyright 2018 Piruin Panichphol
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package me.piruin.spinney.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.util.Locale;
import me.piruin.spinney.Spinney;

public class AdvanceFragment extends Fragment {

  @BindView(R.id.spinney_country) Spinney<DatabaseItem> countrySpinney;
  @BindView(R.id.spinney_region) Spinney<DatabaseItem> regionSpinney;
  @BindView(R.id.spinney_city) Spinney<DatabaseItem> citySpinney;
  @BindView(R.id.spinney_district) Spinney<DatabaseItem> districtSpinney;

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_advance, container, false);
    ButterKnife.bind(this, view);
    return view;
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    useListOfDatabaseItemWithFilterByFeature();
    playWithSafeMode();
  }

  public void useListOfDatabaseItemWithFilterByFeature() {
    countrySpinney.setSearchableItem(Data.country);
    citySpinney.setItemPresenter(
      new Spinney.ItemPresenter<DatabaseItem>() { //Custom item presenter add Spinney
        @Override public String getLabelOf(DatabaseItem item, int position) {
          return String.format(Locale.getDefault(), "%d. %s", position, item.getName());
        }
      });
    citySpinney.setItemCaptionPresenter(new Spinney.ItemPresenter<DatabaseItem>() {
      @Override public String getLabelOf(DatabaseItem item, int position) {
        return countrySpinney.getSelectedItem().getName();
      }
    });
    citySpinney.setSearchableItem(Data.cities);
    districtSpinney.setItems(Data.districts);
    regionSpinney.setItems(Data.regions);

    regionSpinney.filterBy(countrySpinney, new Spinney.Condition<DatabaseItem, DatabaseItem>() {
      @Override public boolean filter(DatabaseItem selectedCountry, DatabaseItem eachRegion) {
        return eachRegion.getParentId() == selectedCountry.getId();
      }
    });
    citySpinney.filterBy(countrySpinney, new Spinney.Condition<DatabaseItem, DatabaseItem>() {
      @Override public boolean filter(DatabaseItem selectedCountry, DatabaseItem eachCity) {
        return eachCity.getParentId() == selectedCountry.getId();
      }
    });
    districtSpinney.filterBy(citySpinney, new Spinney.Condition<DatabaseItem, DatabaseItem>() {
      @Override public boolean filter(DatabaseItem selectedCity, DatabaseItem eachDistrict) {
        return eachDistrict.getParentId() == selectedCity.getId();
      }
    });

    //setSelectedItem() of parent Spinney must call after filterBy()
    countrySpinney.setSelectedItem(new DatabaseItem(1, "THAILAND"));
  }

  private void playWithSafeMode() {
    try {
      //below will cause IllegalArgumentException cause not enableSafeMode
      countrySpinney.setSelectedItem(new DatabaseItem(2000, "METROPOLIS"));
    } catch (IllegalArgumentException ignore) {
    }

    citySpinney.setSafeModeEnable(true);
    citySpinney.setSelectedItem(new DatabaseItem(60, "GOTHAM"));
  }

  @OnClick(R.id.clear) void onClearClick() {
    countrySpinney.clearSelection();
  }
}


/*
 * Copyright 2018 Piruin Panichphol
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package me.piruin.spinney.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.piruin.spinney.Spinney;

public class BasicFragment extends Fragment {

  @BindView(R.id.spinney_searchable) Spinney<String> searchable;
  @BindView(R.id.spinney_normal) Spinney<String> normal;
  @BindView(R.id.spinney_typeItem) Spinney<DatabaseItem> typeItem;

  @Nullable @Override public View onCreateView(
    @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState)
  {
    View view = inflater.inflate(R.layout.fragment_basic, container, false);
    ButterKnife.bind(this, view);
    return view;
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    normal.setItems(Data.department);

    searchable.setSearchableItem(Data.department);
    searchable.setOnItemSelectedListener(new Spinney.OnItemSelectedListener<String>() {
      @Override public void onItemSelected(Spinney view, String selectedItem, int position) {
        normal.clearSelection();
      }
    });

    typeItem.setItemPresenter(new Spinney.ItemPresenter<DatabaseItem>() {
      @Override public String getLabelOf(DatabaseItem item, int position) {
        return item.getName();
      }
    });
    typeItem.setItemCaptionPresenter(new Spinney.ItemPresenter<DatabaseItem>() {
      @Override public String getLabelOf(DatabaseItem item, int position) {
        return item.getParentId() + "-" + item.getId();
      }
    });
    typeItem.setSearchableItem(Data.cities);
  }
}


/*
 * Copyright 2017 Piruin Panichphol
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package me.piruin.spinney.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SampleActivity extends AppCompatActivity {

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sample);
  }
}


package me.piruin.spinney;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import me.piruin.spinney.Spinney.ItemPresenter;

public final class SpinneyAdapter<T> extends BaseAdapter implements Filterable {

  private final Context context;
  private final int layoutId;
  private final ItemPresenter presenter;
  private List<T> originalItems;
  private List<T> conditionedItem;
  private List<T> filteredItem;
  private ItemPresenter captionPresenter;
  private boolean isDependencyMode;

  public SpinneyAdapter(Context context, List<T> items) {
    this(context, items, Spinney.defaultItemPresenter);
  }

  public SpinneyAdapter(Context context, List<T> items, ItemPresenter presenter) {
    this(context, R.layout.spinney_item, items, presenter);
  }

  public SpinneyAdapter(Context context, @LayoutRes int layoutId, List<T> items,
    ItemPresenter presenter) {
    super();
    this.context = context;
    this.layoutId = layoutId;
    this.originalItems = items;
    this.conditionedItem = new ArrayList<>(items);
    this.filteredItem = new ArrayList<>(items);
    this.presenter = presenter;
  }

  public void setCaptionPresenter(@Nullable ItemPresenter captionPresenter) {
    this.captionPresenter = captionPresenter;
  }

  @Override public int getCount() {
    return filteredItem.size();
  }

  @Override public long getItemId(int position) {
    return getItem(position).hashCode();
  }

  @Override public Object getItem(int position) {
    return filteredItem.get(position);
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater mInflater =
      (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    Holder holder;
    if (convertView == null) {
      convertView = mInflater.inflate(layoutId, parent, false);
      holder = new Holder(convertView);
      convertView.setTag(holder);
    } else {
      holder = (Holder) convertView.getTag();
    }
    Object item = getItem(position);
    String label = presenter.getLabelOf(item, position);
    String caption = null;
    if (captionPresenter != null) caption = captionPresenter.getLabelOf(item, position);
    holder.bind(label, caption);

    return convertView;
  }

  void clearCondition() {
    if (isDependencyMode) {
      conditionedItem = new ArrayList<>();
      filteredItem = new ArrayList<>();
    } else {
      conditionedItem = new ArrayList<>(originalItems);
      filteredItem = new ArrayList<>(conditionedItem);
    }
    notifyDataSetChanged();
  }

  <K> void updateCondition(@NonNull K parentItem, Spinney.Condition<T, K> condition) {
    conditionedItem = new ArrayList<>();
    for (T item : originalItems) {
      if (condition.filter(parentItem, item)) conditionedItem.add(item);
    }
    filteredItem = new ArrayList<>(conditionedItem);
    notifyDataSetChanged();
  }

  @Override public Filter getFilter() {
    return new FilterByLabel();
  }

  /**
   * @param item to find position
   * @return position (index) of item on original items list
   */
  public int findPositionOf(T item) {
    return originalItems.indexOf(item);
  }

  public boolean isFilteredListContain(@Nullable T item) {
    return filteredItem.contains(item);
  }

  void setDependencyMode(boolean isDependencyMode) {
    this.isDependencyMode = isDependencyMode;
  }

  private static class Holder {
    private final TextView line1;
    private final TextView line2;

    Holder(View itemView) {
      line1 = itemView.findViewById(R.id.spinney_item_line1);
      line2 = itemView.findViewById(R.id.spinney_item_line2);
    }

    void bind(String line1, @Nullable String line2) {
      this.line1.setText(line1);
      if (line2 != null) {
        this.line2.setText(line2);
        this.line2.setVisibility(View.VISIBLE);
      } else {
        this.line2.setVisibility(View.GONE);
      }
    }
  }

  private class FilterByLabel extends Filter {

    private final Locale locale = Locale.getDefault();

    @Override protected FilterResults performFiltering(final CharSequence constraint) {
      FilterResults results = new FilterResults();
      if (TextUtils.isEmpty(constraint)) {
        results.values = conditionedItem;
        results.count = conditionedItem.size();
      } else {
        List<T> filteredList = new ArrayList<>();
        String query = constraint.toString().toLowerCase(locale);
        for (T item : conditionedItem) {
          if (isFound(presenter, item, query) || isFound(captionPresenter, item, query)) {
            filteredList.add(item);
          }
        }
        results.values = filteredList;
        results.count = filteredList.size();
      }
      return results;
    }

    private boolean isFound(@Nullable ItemPresenter presenter, Object object, String query) {
      return presenter != null && presenter.getLabelOf(object, 0)
        .toLowerCase(locale)
        .contains(query);
    }

    @Override protected void publishResults(CharSequence constraint, final FilterResults results) {
      filteredItem = (List<T>) results.values;
      notifyDataSetChanged();
    }
  }
}


/*
 * Copyright 2017 Piruin Panichphol
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package me.piruin.spinney;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Filterable;
import android.widget.ListView;
import java.io.Serializable;

/**
 * Dialog with SearchView and ListView design for represent a lot items with filterable function.
 * This dialog use as default dialog for searchable mode of Spinney but it also can use separately
 * as easy as use ordinary dialog
 */
public class SpinneyDialog extends Dialog {

  private OnItemSelectedListener onItemSelectedListener;

  private final SearchView searchView;
  private final ListView listViewItems;

  public SpinneyDialog(Context context) {
    super(context);

    setContentView(R.layout.spinney_dialog);
    searchView = findViewById(R.id.spinney_search);
    searchView.setIconifiedByDefault(false);
    searchView.setOnQueryTextListener(new OnQueryTextListener() {
      @Override public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        return true;
      }

      @Override public boolean onQueryTextChange(String query) {
        if (TextUtils.isEmpty(query)) {
          ((Filterable) listViewItems.getAdapter()).getFilter().filter(null);
        } else {
          ((Filterable) listViewItems.getAdapter()).getFilter().filter(query);
        }
        return true;
      }
    });
    searchView.setOnCloseListener(new OnCloseListener() {
      @Override public boolean onClose() { return false; }
    });
    searchView.clearFocus();

    listViewItems = findViewById(R.id.spinney_list);

    hindSoftKeyboard(context);
  }

  private void hindSoftKeyboard(Context context) {
    InputMethodManager mgr =
      (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    mgr.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
  }

  /**
   * @param onItemSelectedListener to callback when item was selected
   */
  public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
    this.onItemSelectedListener = onItemSelectedListener;
  }

  /**
   * Adapter of item to present on dialog
   *
   * @param adapter to show on ListView of Dialog
   */
  public void setAdapter(final SpinneyAdapter adapter) {
    listViewItems.setAdapter(adapter);
    listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object selectedItem = parent.getItemAtPosition(position);
        boolean shouldDismiss = onItemSelectedListener.onItemSelected(
          selectedItem,
          adapter.findPositionOf(selectedItem));
        if (shouldDismiss)
          dismiss();
      }
    });
  }

  /** @param hint to use as hint on at SearchView of dialog */
  public final void setHint(CharSequence hint) {
    searchView.setQueryHint(hint);
  }

  /**
   * Callback to handle when item of SpinneyDialog was selected
   *
   * @param <T> type of Item
   */
  public interface OnItemSelectedListener<T> extends Serializable {

    /**
     * @param item that have been selected
     * @param position of selected item in original list Not filtered list!
     * @return whether should dialog close itself or not
     */
    boolean onItemSelected(T item, int position);
  }
}


/*
 * Copyright 2017 Piruin Panichphol
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package me.piruin.spinney;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Replacement of vanilla Spinner with Super-power
 *
 * @param <T> Type of Selectable choice to use with Spinney
 */
public class Spinney<T> extends AppCompatEditText {

  static ItemPresenter defaultItemPresenter = new ItemPresenter() {
    @Override public String getLabelOf(Object item, int position) {
      return item.toString();
    }
  };
  private static boolean defaultSafeMode = false;
  private final CharSequence hint;
  /** Dialog object to show selectable item of Spinney can be Searchable or normal List Dialog */
  private Dialog dialog;
  /** OnItemSelectedListener set by Library user */
  private OnItemSelectedListener<T> itemSelectedListener;
  /** Internal OnItemSelectedListeners use when filterBy() was called */
  private List<OnItemSelectedListener<T>> _itemSelectedListeners = new ArrayList<>();
  private ItemPresenter itemPresenter = defaultItemPresenter;
  private ItemPresenter itemCaptionPresenter;
  private SpinneyAdapter<T> adapter;
  private T selectedItem;
  private boolean safeMode = defaultSafeMode;

  public Spinney(Context context) {
    this(context, null);
  }

  public Spinney(Context context, AttributeSet attrs) {
    this(context, attrs, android.R.attr.editTextStyle);
  }

  public Spinney(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    /*
      Save hint at constructor because, after this getHint() will return null
      when use Spinney as child of Support's TextInputLayout.
     */
    hint = getHint();
  }

  /**
   * <pre>
   * Enable safe mode to all spinney use in Application by default.
   * By the way, only use this in case of emergency
   * </pre>
   *
   * @param enable or disable safe mode
   */
  public static void enableSafeModeByDefault(boolean enable) {
    defaultSafeMode = enable;
  }

  /**
   * replace default global ItemPresenter this should be set at Application.onCreate()
   *
   * @param defaultItemDisplayer to present selected object on spinney view
   */
  public static void setDefaultItemPresenter(@NonNull ItemPresenter defaultItemDisplayer) {
    Spinney.defaultItemPresenter = defaultItemDisplayer;
  }

  /**
   * <pre>
   * Use this when number of items is more than user can scan by their eye.
   *
   * This method use inpurt list of item to create SpinneyAdapter if want to custom.
   * See setSearchableAdapter(SpinneyAdpter)
   * </pre>
   *
   * @param items list of item use
   */
  public final void setSearchableItem(@NonNull final List<T> items) {
    SpinneyAdapter<T> adapter = new SpinneyAdapter<>(getContext(), items, itemPresenter);
    adapter.setCaptionPresenter(itemCaptionPresenter);
    setSearchableAdapter(adapter);
  }

  /**
   * Call this when build-in SpinneyAdapter not enough for you requirement
   *
   * @param adapter spinneyAdapter to use with SpinneyDialog
   */
  public final void setSearchableAdapter(@NonNull final SpinneyAdapter<T> adapter) {
    this.adapter = adapter;

    SpinneyDialog searchableListDialog = new SpinneyDialog(getContext());
    searchableListDialog.setAdapter(adapter);
    searchableListDialog.setHint(hint);
    searchableListDialog.setOnItemSelectedListener(new SpinneyDialog.OnItemSelectedListener<T>() {
      @Override public boolean onItemSelected(@NonNull Object item, int position) {
        whenItemSelected((T) item, position);
        return true;
      }
    });
    dialog = searchableListDialog;
  }

  private void whenItemSelected(@Nullable T item, int selectedIndex) {
    this.selectedItem = item;
    if (item == null) {
      setText(null);
      for (OnItemSelectedListener _listener : _itemSelectedListeners)
        _listener.onItemSelected(Spinney.this, item, selectedIndex);
    } else {
      setText(itemPresenter.getLabelOf(item, selectedIndex));
      for (OnItemSelectedListener _listener : _itemSelectedListeners)
        _listener.onItemSelected(Spinney.this, item, selectedIndex);
      if (itemSelectedListener != null) {
        itemSelectedListener.onItemSelected(Spinney.this, item, selectedIndex);
      }
    }
  }

  /**
   * enable safeMode to tell Spinney not throw exception when set selectedItem that not found in
   * adapter.
   * not recommend this in app that need consistency
   *
   * @param enable or disable saftmode
   */
  public void setSafeModeEnable(boolean enable) {
    this.safeMode = enable;
  }

  /**
   * Just set List of item on Dialog! Don't worry with Adapter Spinney will handler with it
   *
   * @param items list of item use
   */
  public final void setItems(@NonNull final List<T> items) {
    adapter = new SpinneyAdapter<>(getContext(), items, itemPresenter);
    adapter.setCaptionPresenter(itemCaptionPresenter);
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setTitle(getHint() != null ? getHint() : hint);
    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int selectedIndex) {
        T selectedItem = (T) adapter.getItem(selectedIndex);
        whenItemSelected(selectedItem, adapter.findPositionOf(selectedItem));
      }
    });
    dialog = builder.create();
  }

  /**
   * Set parent spinney and Condition to filter selectable item by selected item of parent Spinney
   * <pre>
   * {@code
   * countrySpinney.setSearchableItem(Data.country);
   * citySpinney.setItems(Data.cities);
   * citySpinney.filterBy(countrySpinney, new Spinney.Condition<DatabaseItem, DatabaseItem<>() {
   *   public boolean filter(DatabaseItem selectedCountry, DatabaseItem eachCity) {
   *     return eachCity.getParentId() == selectedCountry.getId();
   *   }});
   * }
   * </pre>
   *
   * Please note you must setSelectedItem() of parent Spinney after call filterBy()
   *
   * @param parent Spinney that it selected item will affect to this spinney
   * @param filter condition to filter item on this spinney by selected item of parent
   * @param <K> type of item on parent Spinney
   */
  public final <K> void filterBy(Spinney<K> parent, final Condition<T, K> filter) {
    parent._itemSelectedListeners.add(new OnItemSelectedListener<K>() {

      @Override public void onItemSelected(Spinney parent, K parentSelectedItem, int position) {
        if (parentSelectedItem == null) {
          clearSelection();
          adapter.clearCondition();
          return;
        }
        adapter.updateCondition(parentSelectedItem, filter);
        if (!adapter.isFilteredListContain(selectedItem)) {
          clearSelection();
        }
      }
    });
    adapter.setDependencyMode(true);
    adapter.clearCondition();
  }

  public final void clearSelection() {
    whenItemSelected(null, -1);
  }

  /** @return selected item, this may be null */
  @Nullable public final T getSelectedItem() {
    return selectedItem;
  }

  /**
   * Must call after adapter or item have already set also after call <code>filterBy</code>
   *
   * @param item to set as selected item
   * @throws IllegalArgumentException if not found item in adapter of spinney, enableSafeMode() to
   * disable this exception. safeMode is disable by default
   */
  public final void setSelectedItem(@Nullable T item) {
    if (adapter == null) {
      throw new IllegalStateException("Must set adapter or item before call this");
    }

    int positionOf = adapter.findPositionOf(item);
    if (positionOf >= 0) {
      whenItemSelected(item, positionOf);
    } else if (!safeMode) throw new IllegalArgumentException("Not found specify item");
  }

  /**
   * @return check that spinney have item to select or have nothing by filter
   */
  public final boolean isSelectable() {
    return adapter != null && adapter.getCount() > 0;
  }

  @SuppressLint("ClickableViewAccessibility") @Override public final boolean performClick() {
    dialog.show();
    return true;
  }

  @Override protected final void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    setFocusable(false);
    setClickable(true);
    setLongClickable(false);
  }

  /** @return position of selected item, -1 is nothing select */
  public final int getSelectedItemPosition() {
    return adapter.findPositionOf(selectedItem);
  }

  /**
   * This getter may help if you really need it. By the way, Use with CAUTION!
   *
   * @return SpinneyAdapter currently use by Spinney
   */
  public final SpinneyAdapter<T> getAdapter() {
    return adapter;
  }

  /**
   * ItemPresenter to use only on instance of Spinney. Spinney will use global presenter if this not
   * set
   *
   * @param itemPresenter to control how spinney and (Searchable)listDialog represent selectable
   * item  instead of global ItemPresent
   */
  public final void setItemPresenter(@NonNull ItemPresenter<T> itemPresenter) {
    this.itemPresenter = itemPresenter;
  }

  public final void setItemCaptionPresenter(@NonNull ItemPresenter<T> itemPresenter) {
    this.itemCaptionPresenter = itemPresenter;
  }

  /** @param itemSelectedListener to callback when item was selected */
  public final void setOnItemSelectedListener(
    @NonNull OnItemSelectedListener<T> itemSelectedListener) {
    this.itemSelectedListener = itemSelectedListener;
  }

  /**
   * Callback like use with vanilla Spinner
   *
   * @param <T> type of Selectable Item. must be same as type as specify at Spinney object
   */
  public interface OnItemSelectedListener<T> {

    /**
     * @param view Spinney view that fire this method
     * @param selectedItem user selected item
     * @param position at current list
     */
    void onItemSelected(Spinney view, T selectedItem, int position);
  }

  /** Control how item used with Spinney should present as String on Spinney view and Dialog */
  public interface ItemPresenter<T> {

    /**
     * Time to parse item to present on Spinney
     *
     * @param item target item to parse
     * @param position of item when it was select
     * @return represent String of item
     */
    String getLabelOf(T item, int position);
  }

  /**
   * Injectable condition to control whether item should present on  list of Spinney or not!
   *
   * @param <T> Type of item to check
   * @param <K> Type of value to may use as condition to present T
   */
  public interface Condition<T, K> {

    /**
     * @param parentItem selected object of parent spinney may use as Condition to filter item
     * @param item to check whether it should present or not
     * @return true if item should present, false otherwise
     */
    boolean filter(K parentItem, T item);
  }
}


