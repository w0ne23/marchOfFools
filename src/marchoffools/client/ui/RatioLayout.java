package marchoffools.client.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 비율 기반 레이아웃 매니저
 * 컴포넌트들을 지정된 비율에 따라 배치합니다.
 */
public class RatioLayout implements LayoutManager2 {
    
    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;
    
    private int orientation;
    private int gap;
    
    private List<Component> components = new ArrayList<>();
    private Map<Component, Integer> ratios = new HashMap<>();
    
    /**
     * 기본 수직 레이아웃 생성 (간격 0)
     */
    public RatioLayout() {
        this(VERTICAL, 0);
    }
    
    /**
     * 지정된 방향의 레이아웃 생성 (간격 0)
     */
    public RatioLayout(int orientation) {
        this(orientation, 0);
    }
    
    /**
     * 지정된 방향과 간격의 레이아웃 생성
     * @param orientation VERTICAL 또는 HORIZONTAL
     * @param gap 컴포넌트 간 간격 (픽셀)
     */
    public RatioLayout(int orientation, int gap) {
        this.orientation = orientation;
        this.gap = gap;
    }
    
    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints instanceof Integer) {
            components.add(comp);
            ratios.put(comp, (Integer) constraints);
        } else if (constraints == null) {
            components.add(comp);
            ratios.put(comp, 1); // 기본 비율 1
        } else {
            throw new IllegalArgumentException("Constraint must be an Integer (ratio)");
        }
    }
    
    @Override
    public void addLayoutComponent(String name, Component comp) {
        components.add(comp);
        ratios.put(comp, 1); // 기본 비율 1
    }
    
    @Override
    public void removeLayoutComponent(Component comp) {
        components.remove(comp);
        ratios.remove(comp);
    }
    
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int width = 0;
            int height = 0;
            
            for (Component comp : components) {
                if (comp.isVisible()) {
                    Dimension d = comp.getPreferredSize();
                    if (orientation == VERTICAL) {
                        width = Math.max(width, d.width);
                        height += d.height;
                    } else {
                        width += d.width;
                        height = Math.max(height, d.height);
                    }
                }
            }
            
            int visibleCount = (int) components.stream().filter(Component::isVisible).count();
            if (visibleCount > 0) {
                if (orientation == VERTICAL) {
                    height += gap * (visibleCount - 1);
                } else {
                    width += gap * (visibleCount - 1);
                }
            }
            
            return new Dimension(
                width + insets.left + insets.right,
                height + insets.top + insets.bottom
            );
        }
    }
    
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }
    
    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int width = parent.getWidth() - insets.left - insets.right;
            int height = parent.getHeight() - insets.top - insets.bottom;
            
            List<Component> visibleComponents = new ArrayList<>();
            int totalRatio = 0;
            
            for (Component comp : components) {
                if (comp.isVisible()) {
                    visibleComponents.add(comp);
                    totalRatio += ratios.get(comp);
                }
            }
            
            if (visibleComponents.isEmpty() || totalRatio == 0) {
                return;
            }
            
            int totalGap = gap * (visibleComponents.size() - 1);
            int availableSpace = (orientation == VERTICAL ? height : width) - totalGap;
            
            int x = insets.left;
            int y = insets.top;
            
            // 마지막 컴포넌트를 위해 남은 공간 추적
            int remainingSpace = availableSpace;
            int remainingRatio = totalRatio;
            
            for (int i = 0; i < visibleComponents.size(); i++) {
                Component comp = visibleComponents.get(i);
                int ratio = ratios.get(comp);
                
                if (orientation == VERTICAL) {
                    int compHeight;
                    if (i == visibleComponents.size() - 1) {
                        // 마지막 컴포넌트는 남은 공간 모두 사용 (반올림 오차 방지)
                        compHeight = remainingSpace;
                    } else {
                        compHeight = (availableSpace * ratio) / totalRatio;
                        remainingSpace -= compHeight;
                        remainingRatio -= ratio;
                    }
                    comp.setBounds(x, y, width, compHeight);
                    y += compHeight + gap;
                } else {
                    int compWidth;
                    if (i == visibleComponents.size() - 1) {
                        // 마지막 컴포넌트는 남은 공간 모두 사용 (반올림 오차 방지)
                        compWidth = remainingSpace;
                    } else {
                        compWidth = (availableSpace * ratio) / totalRatio;
                        remainingSpace -= compWidth;
                        remainingRatio -= ratio;
                    }
                    comp.setBounds(x, y, compWidth, height);
                    x += compWidth + gap;
                }
            }
        }
    }
    
    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }
    
    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }
    
    @Override
    public void invalidateLayout(Container target) {
        // 캐시가 없으므로 할 일 없음
    }
    
    /**
     * 컴포넌트의 비율 반환
     */
    public int getRatio(Component comp) {
        return ratios.getOrDefault(comp, 1);
    }
    
    /**
     * 컴포넌트의 비율 변경
     */
    public void setRatio(Component comp, int ratio) {
        if (components.contains(comp)) {
            ratios.put(comp, ratio);
        }
    }
}