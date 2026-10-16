package com.said.keyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyboardService extends InputMethodService {

    private boolean isUpperCase = false;
    private final Map<Button, String[]> keyMap = new HashMap<>();
    private Button btnShift;
    private static final List<String> clipboardHistory = new ArrayList<>();

    // Silme için sürekli basılı tutma kontrolü
    private final Handler deleteHandler = new Handler(Looper.getMainLooper());
    private final Runnable deleteRunnable = new Runnable() {
        @Override
        public void run() {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.deleteSurroundingText(1, 0);
                deleteHandler.postDelayed(this, 50); // 50ms aralıkla seri silme
            }
        }
    };

    @Override
    public View onCreateInputView() {
        SharedPreferences prefs = getSharedPreferences("KeyboardPrefs", Context.MODE_PRIVATE);
        int keyboardHeight = prefs.getInt("keyboard_height", 270);
        String bgColorHex = prefs.getString("bg_color", "#1E1E1E");

        View keyboardView = getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboardView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(keyboardHeight)));
        keyboardView.setBackgroundColor(Color.parseColor(bgColorHex));

        // Noktasız harflere basılı tutunca çıkan Türkçe/Özel karakter tanımları
        setupKeyWithPopup(keyboardView, R.id.btn_s, "s", "S", new String[]{"ş", "Ş", "ß", "ś"});
        setupKeyWithPopup(keyboardView, R.id.btn_g, "g", "G", new String[]{"ğ", "Ğ", "ĝ"});
        setupKeyWithPopup(keyboardView, R.id.btn_u, "u", "U", new String[]{"ü", "Ü", "û", "ū", "ú", "ù"});
        setupKeyWithPopup(keyboardView, R.id.btn_o, "o", "O", new String[]{"ö", "Ö", "ô", "ó", "ò"});
        setupKeyWithPopup(keyboardView, R.id.btn_c, "c", "C", new String[]{"ç", "Ç", "ć", "č"});
        setupKeyWithPopup(keyboardView, R.id.btn_i, "ı", "I", new String[]{"i", "İ", "î", "ï", "í"});
        setupKeyWithPopup(keyboardView, R.id.btn_a, "a", "A", new String[]{"â", "ä", "à", "á", "ā"});
        setupKeyWithPopup(keyboardView, R.id.btn_e, "e", "E", new String[]{"é", "è", "ê", "ë", "ē"});

        // Standart Harfler
        setupKey(keyboardView, R.id.btn_q, "q", "Q");
        setupKey(keyboardView, R.id.btn_w, "w", "W");
        setupKey(keyboardView, R.id.btn_r, "r", "R");
        setupKey(keyboardView, R.id.btn_t, "t", "T");
        setupKey(keyboardView, R.id.btn_y, "y", "Y");
        setupKey(keyboardView, R.id.btn_p, "p", "P");
        setupKey(keyboardView, R.id.btn_d, "d", "D");
        setupKey(keyboardView, R.id.btn_f, "f", "F");
        setupKey(keyboardView, R.id.btn_h, "h", "H");
        setupKey(keyboardView, R.id.btn_j, "j", "J");
        setupKey(keyboardView, R.id.btn_k, "k", "K");
        setupKey(keyboardView, R.id.btn_l, "l", "L");
        setupKey(keyboardView, R.id.btn_z, "z", "Z");
        setupKey(keyboardView, R.id.btn_x, "x", "X");
        setupKey(keyboardView, R.id.btn_v, "v", "V");
        setupKey(keyboardView, R.id.btn_b, "b", "B");
        setupKey(keyboardView, R.id.btn_n, "n", "N");
        setupKey(keyboardView, R.id.btn_m, "m", "M");

        // Rakamlar
        setupKey(keyboardView, R.id.btn_1, "1", "1");
        setupKey(keyboardView, R.id.btn_2, "2", "2");
        setupKey(keyboardView, R.id.btn_3, "3", "3");
        setupKey(keyboardView, R.id.btn_4, "4", "4");
        setupKey(keyboardView, R.id.btn_5, "5", "5");
        setupKey(keyboardView, R.id.btn_6, "6", "6");
        setupKey(keyboardView, R.id.btn_7, "7", "7");
        setupKey(keyboardView, R.id.btn_8, "8", "8");
        setupKey(keyboardView, R.id.btn_9, "9", "9");
        setupKey(keyboardView, R.id.btn_0, "0", "0");

        setupKey(keyboardView, R.id.btn_space, " ", " ");
        setupKey(keyboardView, R.id.btn_dot, ".", ".");
        setupKey(keyboardView, R.id.btn_comma, ",", ",");

        // Shift Tuşu
        btnShift = keyboardView.findViewById(R.id.btn_shift);
        btnShift.setOnClickListener(v -> {
            isUpperCase = !isUpperCase;
            btnShift.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isUpperCase ? 0xFF0D6EFD : 0xFF555555));
            updateKeyboardCases();
        });

        // Silme Tuşu - Tek Dokunmada 1 tane, Uzun Basınca Seri Silme
        Button btnDelete = keyboardView.findViewById(R.id.btn_delete);
        btnDelete.setOnTouchListener((v, event) -> {
            InputConnection ic = getCurrentInputConnection();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (ic != null) ic.deleteSurroundingText(1, 0);
                    deleteHandler.postDelayed(deleteRunnable, 400);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    deleteHandler.removeCallbacks(deleteRunnable);
                    break;
            }
            return true;
        });

        // Enter Tuşu
        Button btnEnter = keyboardView.findViewById(R.id.btn_enter);
        btnEnter.setOnClickListener(v -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.sendKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER));
                ic.sendKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER));
            }
        });

        // SYM Tuşu (Tüm Eksiksiz Özel Karakterler ve Semboller)
        Button btnSymbols = keyboardView.findViewById(R.id.btn_symbols);
        btnSymbols.setOnClickListener(v -> showPopupAlternatives(btnSymbols, new String[]{
            "/", "\\", "-", "+", "=", "(", ")", "[", "]", "{", "}", "?", "!", "\"", "'", "#", "$", "%", "*", "_", "&", "|", "<", ">", "~", "^", ";", ":", "@", ",", "."
        }));

        // Emoji Paneli Tuşu (😊)
        Button btnEmoji = keyboardView.findViewById(R.id.btn_emoji);
        btnEmoji.setOnClickListener(v -> showPopupAlternatives(btnEmoji, new String[]{"😊", "😂", "❤️", "🔥", "👍", "😎", "🚀", "💡", "✨", "🎉", "👏", "⭐", "💯", "🙏"}));

        // Pano (Clipboard) Tuşu - Anında Yapıştırmaz, Geçmiş Menüsünü Açıp Sıralar
        Button btnClipboard = keyboardView.findViewById(R.id.btn_clipboard);
        btnClipboard.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clipData = clipboard.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    ClipData.Item item = clipData.getItemAt(0);
                    if (item.getUri() != null) {
                        String imageItem = "[Ekran Görüntüsü/Resim]";
                        if (!clipboardHistory.contains(imageItem)) clipboardHistory.add(0, imageItem);
                    } else if (item.getText() != null) {
                        String textItem = item.getText().toString();
                        if (!clipboardHistory.contains(textItem)) clipboardHistory.add(0, textItem);
                    }
                }
            }
            showClipboardHistoryMenu(btnClipboard);
        });

        // Ayarlar Paneli Tuşu (⚙)
        Button btnSettingsPanel = keyboardView.findViewById(R.id.btn_settings_panel);
        btnSettingsPanel.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        return keyboardView;
    }

    private void setupKey(View parent, int id, String lower, String upper) {
        Button btn = parent.findViewById(id);
        if (btn != null) {
            keyMap.put(btn, new String[]{lower, upper});
            btn.setOnClickListener(v -> {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) {
                    String[] chars = keyMap.get(btn);
                    if (chars != null) {
                        ic.commitText(isUpperCase ? chars[1] : chars[0], 1);
                    }
                }
            });
        }
    }

    private void setupKeyWithPopup(View parent, int id, String lower, String upper, String[] alternatives) {
        Button btn = parent.findViewById(id);
        if (btn != null) {
            keyMap.put(btn, new String[]{lower, upper});
            btn.setOnClickListener(v -> {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) {
                    String[] chars = keyMap.get(btn);
                    if (chars != null) {
                        ic.commitText(isUpperCase ? chars[1] : chars[0], 1);
                    }
                }
            });

            btn.setOnLongClickListener(v -> {
                showPopupAlternatives(btn, alternatives);
                return true;
            });
        }
    }

    private void showPopupAlternatives(Button anchorButton, String[] alternatives) {
        Context context = anchorButton.getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setBackgroundColor(Color.parseColor("#333333"));
        layout.setPadding(8, 8, 8, 8);

        PopupWindow popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        for (String altChar : alternatives) {
            Button altBtn = new Button(context);
            altBtn.setText(altChar);
            altBtn.setTextColor(Color.WHITE);
            altBtn.setTextSize(18);
            altBtn.setBackgroundColor(Color.TRANSPARENT);
            altBtn.setOnClickListener(view -> {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) {
                    ic.commitText(altChar, 1);
                }
                popupWindow.dismiss();
            });
            layout.addView(altBtn);
        }

        popupWindow.showAsDropDown(anchorButton, 0, -anchorButton.getHeight() - 120, Gravity.TOP);
    }

    private void showClipboardHistoryMenu(Button anchorButton) {
        Context context = anchorButton.getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#222222"));
        layout.setPadding(12, 12, 12, 12);

        ScrollView scrollView = new ScrollView(context);
        LinearLayout listLayout = new LinearLayout(context);
        listLayout.setOrientation(LinearLayout.VERTICAL);

        PopupWindow popupWindow = new PopupWindow(scrollView, ViewGroup.LayoutParams.MATCH_PARENT, 400, true);

        if (clipboardHistory.isEmpty()) {
            TextView emptyView = new TextView(context);
            emptyView.setText("Pano geçmişi boş.");
            emptyView.setTextColor(Color.LTGRAY);
            emptyView.setPadding(16, 16, 16, 16);
            listLayout.addView(emptyView);
        } else {
            for (String histItem : clipboardHistory) {
                Button itemBtn = new Button(context);
                itemBtn.setText(histItem);
                itemBtn.setTextColor(Color.WHITE);
                itemBtn.setTextSize(14);
                itemBtn.setBackgroundColor(Color.parseColor("#333333"));
                itemBtn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                itemBtn.setPadding(16, 12, 16, 12);

                itemBtn.setOnClickListener(v -> {
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) {
                        ic.commitText(histItem, 1);
                    }
                    popupWindow.dismiss();
                });
                listLayout.addView(itemBtn);
            }
        }

        scrollView.addView(listLayout);
        popupWindow.setContentView(scrollView);
        popupWindow.showAsDropDown(anchorButton, 0, -anchorButton.getHeight() - 420, Gravity.TOP);
    }

    private void updateKeyboardCases() {
        for (Map.Entry<Button, String[]> entry : keyMap.entrySet()) {
            Button btn = entry.getKey();
            String[] chars = entry.getValue();
            if (chars[0].length() == 1 && Character.isLetter(chars[0].charAt(0))) {
                btn.setText(isUpperCase ? chars[1] : chars[0]);
            }
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
