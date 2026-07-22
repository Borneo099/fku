#!/usr/bin/env python3
"""Fix type conversion issues in decompiled code: double→float, float→int casts."""

import re
import os

SOURCE_DIR = '/workspace/common/src/main/java'

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    
    original = content
    lines = content.split('\n')
    new_lines = []
    
    for line in lines:
        # Fix: vertex(mat, double, double, double) -> vertex(mat, (float), (float), (float))
        # Only fix when the argument is clearly a double variable (minX, maxX, etc.)
        line = re.sub(
            r'(vertex\([^,]+,\s*)(minX|minY|minZ|maxX|maxY|maxZ)(\s*,\s*)(minX|minY|minZ|maxX|maxY|maxZ)(\s*,\s*)(minX|minY|minZ|maxX|maxY|maxZ)',
            r'\1(float)\2\3(float)\4\5(float)\6',
            line
        )
        
        # Fix: fill(double, double, double, double, int) -> fill((int), (int), (int), (int), int)
        # Pattern: fill(someExpr, someExpr, someExpr, someExpr, color)
        line = re.sub(
            r'\.fill\((\d+\.\d*f?)\s*,\s*(\d+\.\d*f?)\s*,\s*(\d+\.\d*f?)\s*,\s*(\d+\.\d*f?)\s*,',
            lambda m: f'.fill({to_int_cast(m.group(1))}, {to_int_cast(m.group(2))}, {to_int_cast(m.group(3))}, {to_int_cast(m.group(4))},',
            line
        )
        
        # Fix: fill(floatVar, floatVar, floatVar, floatVar, color) - when float vars used as int
        line = re.sub(
            r'\.fill\((\s*)(\w+)(\s*,\s*)(\w+)(\s*,\s*)(\w+)(\s*,\s*)(\w+)(\s*,\s*)([a-zA-Z])',
            lambda m: f'.fill{m.group(1)}(int){m.group(2)}{m.group(3)}(int){m.group(4)}{m.group(5)}(int){m.group(6)}{m.group(7)}(int){m.group(8)}{m.group(9)}{m.group(10)}' if is_float_var(m.group(2)) else m.group(0),
            line
        )
        
        new_lines.append(line)
    
    content = '\n'.join(new_lines)
    
    if content != original:
        with open(filepath, 'w') as f:
            f.write(content)
        return True
    return False

def to_int_cast(expr):
    expr = expr.strip()
    if expr.endswith('f'):
        return f'(int)({expr})'
    if '.' in expr:
        return f'(int)({expr})'
    return expr

def is_float_var(name):
    # Common float variable names in decompiled code
    return name in ('x', 'y', 'z', 'w', 'h', 'width', 'height', 'left', 'top', 'right', 'bottom',
                    'barX', 'barY', 'barWidth', 'barHeight', 'textX', 'textY',
                    'minX', 'minY', 'minZ', 'maxX', 'maxY', 'maxZ',
                    'bx', 'by', 'cx', 'cy', 'sp', 'gap', 'margin', 'padding',
                    'alpha', 'red', 'green', 'blue', 'alphaInt')

def fix_box_vars(filepath):
    """Fix AABB box variable declarations: double -> float with cast."""
    with open(filepath, 'r') as f:
        content = f.read()
    
    original = content
    
    # Change: double minX = box.minX; -> float minX = (float)box.minX;
    content = re.sub(
        r'double (minX|minY|minZ|maxX|maxY|maxZ)\s*=\s*box\.\1;',
        r'float \1 = (float)box.\1;',
        content
    )
    
    if content != original:
        with open(filepath, 'w') as f:
            f.write(content)
        return True
    return False

def main():
    total = 0
    for root, dirs, files in os.walk(SOURCE_DIR):
        for file in files:
            if not file.endswith('.java'):
                continue
            filepath = os.path.join(root, file)
            if fix_file(filepath):
                print(f"  Fixed casts in: {filepath}")
                total += 1
            if fix_box_vars(filepath):
                print(f"  Fixed box vars in: {filepath}")
                total += 1
    
    print(f"\nTotal files modified: {total}")

if __name__ == '__main__':
    main()